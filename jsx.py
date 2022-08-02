# -*- coding: utf-8 -*-
"""
    pygments.lexers.jsx
    ~~~~~~~~~~~~~~~~~~~~
    Lexers for JSX formats.
    Based on https://github.com/fcurella/jsx-lexer
"""

import re

from pygments.lexer import bygroups, include, default, using
from pygments.lexers.javascript import JavascriptLexer
from pygments.token import Comment, Name, Operator, Punctuation, String, Text

# Use same tokens as `JavascriptLexer`, but with tags and attributes support
TOKENS = JavascriptLexer.tokens
TOKENS.update(
    {
        "jsx": [
            (r"(<)(/?)(>)", bygroups(Punctuation, Punctuation, Punctuation)), # JSXFragment <>|</>
            (r"(<)([\w]+)(\.?)", bygroups(Punctuation, Name.Tag, Punctuation), "tag"),
            (r"(?<=[^=][>}])[^<>{}()\[\];]+(?=[{<])", Text, "children"),
            (r"(<)(/)([\w]+)(>)", bygroups(Punctuation, Punctuation, Name.Tag, Punctuation)),
            (r"(<)(/)([\w]+)", bygroups(Punctuation, Punctuation, Name.Tag), "fragment") # Same for React.Context
        ],
        "tag": [
            (r"\s+", Text),
            (r"([\w]+\s*)(=)(\s*)", bygroups(Name.Attribute, Operator, Text), "attr"),
            (r"({)(\/\*[\s\S]*?\*\/)(})", bygroups(Punctuation, Comment.Multiline, Punctuation)),
            (r"[{}]+", Punctuation),
            (r"[\w\.]+", Name.Attribute),
            (r"(/?)(\s*)(>)", bygroups(Punctuation, Text, Punctuation), "#pop")
        ],
        "children": [
            (r"(>)(\s*?)([\)]?;)", bygroups(Punctuation, Text, Punctuation), "#pop"),
            include("root")
        ],
        "fragment": [
            (r"(.)([\w]+)", bygroups(Punctuation, Name.Attribute)),
            (r"(>)", bygroups(Punctuation), "#pop")
        ],
        "attr": [
            ("{", Punctuation, "expression"),
            ('".*?"', String, "#pop"),
            ("'.*?'", String, "#pop"),
            default("#pop")
        ],
        "expression": [
            ("{", Punctuation, "#push"),
            ("}", Punctuation, "#pop"),
            include("root")
        ],
    }
)
TOKENS["root"].insert(0, include("jsx"))


class JsxLexer(JavascriptLexer):
    """
    Lexer for JSX.
    """

    name = "JSX"
    aliases = ["jsx", "react"]
    filenames = ["*.jsx", "*.react"]
    mimetypes = ["text/jsx", "text/typescript-jsx"]

    flags = re.MULTILINE | re.DOTALL | re.UNICODE

    tokens = TOKENS