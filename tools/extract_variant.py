#!/usr/bin/env python3
"""Extract a version-specific source tree from stonecutter-conditioned sources.

Understands the block-conditional syntax used in this repo:

    //? if <cond> {          open block (taken if cond matches target)
    //?} else if <cond> {    else-if branch
    //?} else {              else branch
    //?}                     close block
    *///?} ...               close/else markers wrapped in a comment end

Branches that are inactive in the VCS are wrapped in a block comment whose
opener is on the first content line ("/*code...") and whose closer sits on the
marker line (" *///?}"). When such a branch is activated for the target, the
wrapper is stripped. Plain Javadoc (/** ... */) is never touched.

Usage: extract_variant.py <src_root> <dst_root> <mc_version> <loader>
"""
import os
import re
import sys

# *///?}  = comment closer "*/" + marker "//?}" -> three slashes total.
# The "/+" after the stars allows backtracking across the extra slash.
MARKER_PREFIX_RE = re.compile(r"^\*+/+//\?(.*)$")


def classify(line):
    """Return (kind, cond) with kind in {open, else, close, other, None}."""
    s = line.strip()
    if not s:
        return (None, None)
    if s.startswith("//?"):
        rest = s[3:].strip()
    else:
        m = MARKER_PREFIX_RE.match(s)
        if not m:
            return (None, None)
        rest = m.group(1).strip()
    if rest.startswith("}"):
        inner = rest[1:].strip()
        if inner.startswith("else"):
            inner = inner[4:].strip()
            if inner.startswith("if"):
                return ("else", inner[2:].strip().rstrip("{").strip())
            return ("else", None)
        return ("close", None)
    if rest.startswith("if"):
        return ("open", rest[2:].strip().rstrip("{").strip())
    return ("other", rest)


def make_eval(mc, loader):
    def cmp_versions(a, b):
        pa = [int(x) for x in a.split(".")]
        pb = [int(x) for x in b.split(".")]
        n = max(len(pa), len(pb))
        pa += [0] * (n - len(pa))
        pb += [0] * (n - len(pb))
        return (pa > pb) - (pa < pb)

    def eval_single(cond):
        if cond == "fabric":
            return loader == "fabric"
        if cond in ("neoforge", "forge", "forgelike"):
            return loader != "fabric"
        m = re.match(r"^(>=|<=|==|>|<)?\s*(.+)$", cond)
        if not m:
            return False
        op = m.group(1) or "=="
        c = cmp_versions(mc, m.group(2).strip())
        return {">=": c >= 0, "<=": c <= 0, "==": c == 0, ">": c > 0, "<": c < 0}[op]

    def eval_cond(cond):
        if "||" in cond:
            return any(eval_single(c.strip()) for c in cond.split("||"))
        if "&&" in cond:
            return all(eval_single(c.strip()) for c in cond.split("&&"))
        return eval_single(cond)

    return eval_cond


def process_lines(lines, eval_fn):
    out = []
    stack = []  # {'taken': bool, 'skip': bool, 'wrapped': bool}

    def skipping():
        return any(e["skip"] for e in stack)

    i = 0
    n = len(lines)
    while i < n:
        raw = lines[i]
        kind, cond = classify(raw)
        if kind == "open" or kind == "else":
            if kind == "open":
                e = {"taken": False, "skip": True, "wrapped": False}
                stack.append(e)
                take = eval_fn(cond)
            else:
                if not stack:
                    i += 1
                    continue
                e = stack[-1]
                if cond is not None:
                    take = (not e["taken"]) and eval_fn(cond)
                else:
                    take = not e["taken"]
            e["skip"] = not take
            e["wrapped"] = False
            if take:
                e["taken"] = True
            i += 1
            # Activated branch wrapped in /* ... */: strip opener from first line
            if take and i < n:
                ns = lines[i].strip()
                if ns.startswith("/*") and not ns.startswith("/**"):
                    e["wrapped"] = True
                    stripped = re.sub(r"^(\s*)/\*", r"\1", lines[i], count=1)
                    if stripped.strip():
                        out.append(stripped)
                    i += 1
            continue
        if kind == "close":
            if stack:
                stack.pop()
            i += 1
            continue
        if skipping():
            i += 1
            continue
        if stack and stack[-1]["wrapped"]:
            # strip optional leading '*' comment decoration from wrapped lines
            out.append(re.sub(r"^(\s*)\*(?!\*)\s?", r"\1", raw, count=1)
                       if raw.lstrip().startswith("*") and not raw.lstrip().startswith("*/")
                       else raw)
        else:
            out.append(raw)
        i += 1
    return out


def main():
    src_root, dst_root, mc, loader = sys.argv[1:5]
    dst_root = os.path.abspath(dst_root)
    eval_fn = make_eval(mc, loader)
    count = 0
    for dirpath, _dirnames, filenames in os.walk(src_root):
        for fn in filenames:
            sp = os.path.join(dirpath, fn)
            rel = os.path.relpath(sp, src_root)
            dp = os.path.join(dst_root, rel)
            with open(sp, "rb") as f:
                data = f.read()
            try:
                text = data.decode("utf-8")
            except UnicodeDecodeError:
                os.makedirs(os.path.dirname(dp), exist_ok=True)
                with open(dp, "wb") as f:
                    f.write(data)
                continue
            out = process_lines(text.split("\n"), eval_fn)
            content = "\n".join(out)
            if not content.strip():
                continue  # branch not taken for this target: skip file
            os.makedirs(os.path.dirname(dp), exist_ok=True)
            with open(dp, "w") as f:
                f.write(content)
            count += 1
    print(f"processed {count} files -> {dst_root}")


if __name__ == "__main__":
    main()