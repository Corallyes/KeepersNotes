"""
TXT structured parser — outputs DocumentNode JSON array.

Each node:
  {"type":"heading","level":1,"content":"Chapter Title","order":0}
  {"type":"paragraph","content":"Body text","order":1}
  {"type":"quote","content":"Quoted text","order":2}
  {"type":"list_item","content":"List item","level":0,"order":3}

Features:
  - Automatic encoding detection (UTF-8, GBK, GB2312, GB18030, etc.)
  - Smart heading detection (Markdown, Chinese, English patterns)
  - Quote block detection
  - List item detection
  - Paragraph grouping
"""

import json
import os
import re
import sys

# Try to import chardet for encoding detection
try:
    import chardet
    HAS_CHARDET = True
except ImportError:
    HAS_CHARDET = False


def parse_txt(file_path):
    """Parse a TXT file and return a JSON array of DocumentNode dicts."""
    try:
        # Verify file exists and has content
        if not os.path.exists(file_path):
            return json.dumps([{"type": "paragraph", "content": "[File not found]", "order": 0}], ensure_ascii=False)

        file_size = os.path.getsize(file_path)
        if file_size == 0:
            return json.dumps([{"type": "paragraph", "content": "[Empty file]", "order": 0}], ensure_ascii=False)

        # Read file with encoding detection
        content = _read_file_smart(file_path)
        if not content or not content.strip():
            return json.dumps([{"type": "paragraph", "content": "[Content empty after decode]", "order": 0}], ensure_ascii=False)

        lines = content.split('\n')
        nodes = []
        order = 0
        current_paragraph = []

        for line in lines:
            stripped = line.strip()

            # Skip empty lines (but use them as paragraph separators)
            if not stripped:
                if current_paragraph:
                    text = '\n'.join(current_paragraph)
                    if text.strip():
                        nodes.append({
                            "type": "paragraph",
                            "content": text,
                            "order": order
                        })
                        order += 1
                    current_paragraph = []
                continue

            # Check for heading
            heading_level = _detect_heading_level(stripped)
            if heading_level > 0:
                # Save any pending paragraph
                if current_paragraph:
                    text = '\n'.join(current_paragraph)
                    if text.strip():
                        nodes.append({
                            "type": "paragraph",
                            "content": text,
                            "order": order
                        })
                        order += 1
                    current_paragraph = []

                nodes.append({
                    "type": "heading",
                    "level": heading_level,
                    "content": _extract_heading_title(stripped),
                    "order": order
                })
                order += 1
                continue

            # Check for quote block
            if _is_quote_line(stripped):
                # Save any pending paragraph
                if current_paragraph:
                    text = '\n'.join(current_paragraph)
                    if text.strip():
                        nodes.append({
                            "type": "paragraph",
                            "content": text,
                            "order": order
                        })
                        order += 1
                    current_paragraph = []

                nodes.append({
                    "type": "quote",
                    "content": _extract_quote_content(stripped),
                    "order": order
                })
                order += 1
                continue

            # Check for list item
            list_info = _detect_list_item(stripped)
            if list_info:
                # Save any pending paragraph
                if current_paragraph:
                    text = '\n'.join(current_paragraph)
                    if text.strip():
                        nodes.append({
                            "type": "paragraph",
                            "content": text,
                            "order": order
                        })
                        order += 1
                    current_paragraph = []

                nodes.append({
                    "type": "list_item",
                    "content": list_info["content"],
                    "level": list_info["level"],
                    "order": order
                })
                order += 1
                continue

            # Regular text line - add to current paragraph
            current_paragraph.append(stripped)

        # Don't forget the last paragraph
        if current_paragraph:
            text = '\n'.join(current_paragraph)
            if text.strip():
                nodes.append({
                    "type": "paragraph",
                    "content": text,
                    "order": order
                })

        if not nodes:
            # Fallback: treat entire content as one paragraph
            nodes.append({
                "type": "paragraph",
                "content": content.strip(),
                "order": 0
            })

        return json.dumps(nodes, ensure_ascii=False)
    except Exception as e:
        return json.dumps([{"type": "paragraph", "content": "[Error: %s]" % str(e), "order": 0}], ensure_ascii=False)


def _read_file_smart(file_path):
    """Read file with automatic encoding detection."""
    # Try common encodings in order
    encodings = ['utf-8-sig', 'utf-8', 'gbk', 'gb2312', 'gb18030', 'big5', 'latin-1']

    # First, try to detect encoding with chardet if available
    if HAS_CHARDET:
        try:
            with open(file_path, 'rb') as f:
                raw_data = f.read()
            if raw_data:
                result = chardet.detect(raw_data)
                if result and result.get('encoding') and result.get('confidence', 0) > 0.7:
                    detected_enc = result['encoding']
                    try:
                        decoded = raw_data.decode(detected_enc)
                        if decoded and '�' not in decoded[:1000]:
                            return decoded
                    except (UnicodeDecodeError, LookupError):
                        pass
        except Exception:
            pass

    # Read raw bytes
    try:
        with open(file_path, 'rb') as f:
            raw_data = f.read()
    except Exception:
        return ""

    if not raw_data:
        return ""

    # Check for BOM
    if raw_data[:3] == b'\xef\xbb\xbf':
        try:
            return raw_data[3:].decode('utf-8')
        except UnicodeDecodeError:
            pass
    if raw_data[:2] == b'\xff\xfe':
        try:
            return raw_data[2:].decode('utf-16-le')
        except UnicodeDecodeError:
            pass
    if raw_data[:2] == b'\xfe\xff':
        try:
            return raw_data[2:].decode('utf-16-be')
        except UnicodeDecodeError:
            pass

    # Try encodings in order
    for encoding in encodings:
        try:
            decoded = raw_data.decode(encoding)
            # Validate: check for replacement characters
            if decoded and '�' not in decoded[:2000]:
                return decoded
        except (UnicodeDecodeError, LookupError):
            continue

    # Last resort: utf-8 with replacement
    try:
        return raw_data.decode('utf-8', errors='replace')
    except Exception:
        return raw_data.decode('latin-1', errors='replace')


def _detect_heading_level(line):
    """Detect heading level from line content. Returns 0 if not a heading."""
    if not line:
        return 0

    # Markdown headings: # ## ### etc.
    md_match = re.match(r'^(#{1,6})\s+', line)
    if md_match:
        return len(md_match.group(1))

    # Chinese chapter markers: 第一章, 第2节, 第三篇
    if re.match(r'^第[一二三四五六七八九十百千\d]+[章节部篇幕]', line):
        return 1

    # Chinese numbered: 一、二、三、
    if re.match(r'^[一二三四五六七八九十]+[、.．]', line):
        return 1

    # Chinese parenthesized: （一）（二）
    if re.match(r'^（[一二三四五六七八九十]+）', line):
        return 2

    # English chapter: Chapter 1, Section 2, Part 3
    if re.match(r'^(Chapter|Section|Part)\s+\d+', line, re.IGNORECASE):
        return 1

    # Numbered headings: 1. 2. 3.
    if re.match(r'^\d+\.\s+', line):
        return 2

    # Sub-numbered: 1.1. 2.3.
    if re.match(r'^\d+\.\d+\.\s+', line):
        return 3

    # Sub-sub-numbered: 1.1.1. 2.3.4.
    if re.match(r'^\d+\.\d+\.\d+\.\s+', line):
        return 4

    # Parenthesized numbers: (1) (2)
    if re.match(r'^\(\d+\)', line):
        return 3

    # Chinese parenthesized numbers: （1）（2）
    if re.match(r'^（\d+）', line):
        return 3

    # All caps English headings (at least 6 chars)
    if re.match(r'^[A-Z][A-Z\s]{5,}$', line):
        return 2

    # Separator lines: --- === ***
    if re.match(r'^[-=*_—]{3,}$', line):
        return 1

    # TRPG-style markers: 【线索】【NPC】
    if re.match(r'^【[一-龥]+】', line):
        return 2

    return 0


def _extract_heading_title(line):
    """Extract title from heading line, removing markers."""
    # Remove Markdown heading markers
    if re.match(r'^#{1,6}\s+', line):
        return re.sub(r'^#{1,6}\s+', '', line).strip()

    # Remove separator lines
    if re.match(r'^[-=*_—]{3,}$', line):
        return "分隔线"

    return line.strip()


def _is_quote_line(line):
    """Check if line is a quote block."""
    # Standard quote markers
    if line.startswith('>') or line.startswith('>>'):
        return True
    # Chinese quote markers
    if line.startswith('「') or line.startswith('『'):
        return True
    return False


def _extract_quote_content(line):
    """Extract content from quote line."""
    # Remove standard quote markers
    if line.startswith('>'):
        return re.sub(r'^>+\s*', '', line).strip()
    # Remove Chinese quote markers
    if line.startswith('「') or line.startswith('『'):
        return line[1:].rstrip('」』').strip()
    return line.strip()


def _detect_list_item(line):
    """Detect if line is a list item. Returns dict with level and content, or None."""
    # Unordered list: - * •
    if re.match(r'^[-*•]\s+', line):
        return {"level": 0, "content": re.sub(r'^[-*•]\s+', '', line).strip()}

    # Ordered list: 1. 2. 3.
    if re.match(r'^\d+\.\s+', line):
        return {"level": 0, "content": re.sub(r'^\d+\.\s+', '', line).strip()}

    # Chinese ordered: ① ② ③
    if re.match(r'^[①②③④⑤⑥⑦⑧⑨⑩]', line):
        return {"level": 0, "content": line[1:].strip()}

    # Parenthesized: (1) (2)
    if re.match(r'^\(\d+\)\s*', line):
        return {"level": 0, "content": re.sub(r'^\(\d+\)\s*', '', line).strip()}

    # Sub-list items (indented)
    if re.match(r'^\s+[-*•]\s+', line):
        return {"level": 1, "content": re.sub(r'^\s+[-*•]\s+', '', line).strip()}

    return None


if __name__ == '__main__':
    # For testing
    if len(sys.argv) > 1:
        result = parse_txt(sys.argv[1])
        print(result)
