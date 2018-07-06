package xyz.hyperreal.highlighter.highlighters

import xyz.hyperreal.backslash._
import xyz.hyperreal.highlighter._

import scala.util.parsing.input.OffsetPosition


object CSSHighlighter extends Highlighter {

  def define =
    Definition(List(
      InfoItems(List(Name("CSS"))), 
      Templates(Map(
        "default" -> GroupAST(Vector(
          LiteralAST("<span class=\""), 
          VariableAST("class"), 
          LiteralAST("\">"), 
          CommandAST(
            pos = OffsetPosition(
              source = "<span class=\"\\class\">\\escape\\text</span>",
              offset = 21
            ),
            c = xyz.hyperreal.backslash.Command.standard("escape"),
            args = List(VariableAST("text")),
            optional = Map()
          ), 
          LiteralAST("</span>")
        ))
      )), 
      Equates(Map(
        "_color_keywords" -> ListRAST(List(
          LiteralRAST("aliceblue"), 
          LiteralRAST("antiquewhite"), 
          LiteralRAST("aqua"), 
          LiteralRAST("aquamarine"), 
          LiteralRAST("azure"), 
          LiteralRAST("beige"), 
          LiteralRAST("bisque"), 
          LiteralRAST("black"), 
          LiteralRAST("blanchedalmond"), 
          LiteralRAST("blue"), 
          LiteralRAST("blueviolet"), 
          LiteralRAST("brown"), 
          LiteralRAST("burlywood"), 
          LiteralRAST("cadetblue"), 
          LiteralRAST("chartreuse"), 
          LiteralRAST("chocolate"), 
          LiteralRAST("coral"), 
          LiteralRAST("cornflowerblue"), 
          LiteralRAST("cornsilk"), 
          LiteralRAST("crimson"), 
          LiteralRAST("cyan"), 
          LiteralRAST("darkblue"), 
          LiteralRAST("darkcyan"), 
          LiteralRAST("darkgoldenrod"), 
          LiteralRAST("darkgray"), 
          LiteralRAST("darkgreen"), 
          LiteralRAST("darkgrey"), 
          LiteralRAST("darkkhaki"), 
          LiteralRAST("darkmagenta"), 
          LiteralRAST("darkolivegreen"), 
          LiteralRAST("darkorange"), 
          LiteralRAST("darkorchid"), 
          LiteralRAST("darkred"), 
          LiteralRAST("darksalmon"), 
          LiteralRAST("darkseagreen"), 
          LiteralRAST("darkslateblue"), 
          LiteralRAST("darkslategray"), 
          LiteralRAST("darkslategrey"), 
          LiteralRAST("darkturquoise"), 
          LiteralRAST("darkviolet"), 
          LiteralRAST("deeppink"), 
          LiteralRAST("deepskyblue"), 
          LiteralRAST("dimgray"), 
          LiteralRAST("dimgrey"), 
          LiteralRAST("dodgerblue"), 
          LiteralRAST("firebrick"), 
          LiteralRAST("floralwhite"), 
          LiteralRAST("forestgreen"), 
          LiteralRAST("fuchsia"), 
          LiteralRAST("gainsboro"), 
          LiteralRAST("ghostwhite"), 
          LiteralRAST("gold"), 
          LiteralRAST("goldenrod"), 
          LiteralRAST("gray"), 
          LiteralRAST("green"), 
          LiteralRAST("greenyellow"), 
          LiteralRAST("grey"), 
          LiteralRAST("honeydew"), 
          LiteralRAST("hotpink"), 
          LiteralRAST("indianred"), 
          LiteralRAST("indigo"), 
          LiteralRAST("ivory"), 
          LiteralRAST("khaki"), 
          LiteralRAST("lavender"), 
          LiteralRAST("lavenderblush"), 
          LiteralRAST("lawngreen"), 
          LiteralRAST("lemonchiffon"), 
          LiteralRAST("lightblue"), 
          LiteralRAST("lightcoral"), 
          LiteralRAST("lightcyan"), 
          LiteralRAST("lightgoldenrodyellow"), 
          LiteralRAST("lightgray"), 
          LiteralRAST("lightgreen"), 
          LiteralRAST("lightgrey"), 
          LiteralRAST("lightpink"), 
          LiteralRAST("lightsalmon"), 
          LiteralRAST("lightseagreen"), 
          LiteralRAST("lightskyblue"), 
          LiteralRAST("lightslategray"), 
          LiteralRAST("lightslategrey"), 
          LiteralRAST("lightsteelblue"), 
          LiteralRAST("lightyellow"), 
          LiteralRAST("lime"), 
          LiteralRAST("limegreen"), 
          LiteralRAST("linen"), 
          LiteralRAST("magenta"), 
          LiteralRAST("maroon"), 
          LiteralRAST("mediumaquamarine"), 
          LiteralRAST("mediumblue"), 
          LiteralRAST("mediumorchid"), 
          LiteralRAST("mediumpurple"), 
          LiteralRAST("mediumseagreen"), 
          LiteralRAST("mediumslateblue"), 
          LiteralRAST("mediumspringgreen"), 
          LiteralRAST("mediumturquoise"), 
          LiteralRAST("mediumvioletred"), 
          LiteralRAST("midnightblue"), 
          LiteralRAST("mintcream"), 
          LiteralRAST("mistyrose"), 
          LiteralRAST("moccasin"), 
          LiteralRAST("navajowhite"), 
          LiteralRAST("navy"), 
          LiteralRAST("oldlace"), 
          LiteralRAST("olive"), 
          LiteralRAST("olivedrab"), 
          LiteralRAST("orange"), 
          LiteralRAST("orangered"), 
          LiteralRAST("orchid"), 
          LiteralRAST("palegoldenrod"), 
          LiteralRAST("palegreen"), 
          LiteralRAST("paleturquoise"), 
          LiteralRAST("palevioletred"), 
          LiteralRAST("papayawhip"), 
          LiteralRAST("peachpuff"), 
          LiteralRAST("peru"), 
          LiteralRAST("pink"), 
          LiteralRAST("plum"), 
          LiteralRAST("powderblue"), 
          LiteralRAST("purple"), 
          LiteralRAST("rebeccapurple"), 
          LiteralRAST("red"), 
          LiteralRAST("rosybrown"), 
          LiteralRAST("royalblue"), 
          LiteralRAST("saddlebrown"), 
          LiteralRAST("salmon"), 
          LiteralRAST("sandybrown"), 
          LiteralRAST("seagreen"), 
          LiteralRAST("seashell"), 
          LiteralRAST("sienna"), 
          LiteralRAST("silver"), 
          LiteralRAST("skyblue"), 
          LiteralRAST("slateblue"), 
          LiteralRAST("slategray"), 
          LiteralRAST("slategrey"), 
          LiteralRAST("snow"), 
          LiteralRAST("springgreen"), 
          LiteralRAST("steelblue"), 
          LiteralRAST("tan"), 
          LiteralRAST("teal"), 
          LiteralRAST("thistle"), 
          LiteralRAST("tomato"), 
          LiteralRAST("turquoise"), 
          LiteralRAST("violet"), 
          LiteralRAST("wheat"), 
          LiteralRAST("white"), 
          LiteralRAST("whitesmoke"), 
          LiteralRAST("yellow"), 
          LiteralRAST("yellowgreen"), 
          LiteralRAST("transparent")
        )), 
        "_vendor_prefixes" -> ListRAST(List(
          LiteralRAST("-ms-"), 
          LiteralRAST("mso-"), 
          LiteralRAST("-moz-"), 
          LiteralRAST("-o-"), 
          LiteralRAST("-xv-"), 
          LiteralRAST("-atsc-"), 
          LiteralRAST("-wap-"), 
          LiteralRAST("-khtml-"), 
          LiteralRAST("-webkit-"), 
          LiteralRAST("prince-"), 
          LiteralRAST("-ah-"), 
          LiteralRAST("-hp-"), 
          LiteralRAST("-ro-"), 
          LiteralRAST("-rim-"), 
          LiteralRAST("-tc-")
        )), 
        "_time_units" -> ListRAST(List(
          LiteralRAST("s"), 
          LiteralRAST("ms")
        )), 
        "_angle_units" -> ListRAST(List(
          LiteralRAST("deg"), 
          LiteralRAST("grad"), 
          LiteralRAST("rad"), 
          LiteralRAST("turn")
        )), 
        "_resolution_units" -> ListRAST(List(
          LiteralRAST("dpi"), 
          LiteralRAST("dpcm"), 
          LiteralRAST("dppx")
        )), 
        "_frequency_units" -> ListRAST(List(
          LiteralRAST("Hz"), 
          LiteralRAST("kHz")
        )), 
        "_all_units" -> BinaryRAST(
          l = BinaryRAST(
            l = BinaryRAST(
              l = BinaryRAST(
                l = VariableRAST("_angle_units"),
                op = "+",
                r = VariableRAST("_frequency_units")
              ),
              op = "+",
              r = VariableRAST("_length_units")
            ),
            op = "+",
            r = VariableRAST("_resolution_units")
          ),
          op = "+",
          r = VariableRAST("_time_units")
        ), 
        "_other_keyword_values" -> ListRAST(List(
          LiteralRAST("above"), 
          LiteralRAST("aural"), 
          LiteralRAST("behind"), 
          LiteralRAST("bidi-override"), 
          LiteralRAST("center-left"), 
          LiteralRAST("center-right"), 
          LiteralRAST("cjk-ideographic"), 
          LiteralRAST("continuous"), 
          LiteralRAST("crop"), 
          LiteralRAST("cross"), 
          LiteralRAST("embed"), 
          LiteralRAST("far-left"), 
          LiteralRAST("far-right"), 
          LiteralRAST("fast"), 
          LiteralRAST("faster"), 
          LiteralRAST("hebrew"), 
          LiteralRAST("high"), 
          LiteralRAST("higher"), 
          LiteralRAST("hiragana"), 
          LiteralRAST("hiragana-iroha"), 
          LiteralRAST("katakana"), 
          LiteralRAST("katakana-iroha"), 
          LiteralRAST("landscape"), 
          LiteralRAST("left-side"), 
          LiteralRAST("leftwards"), 
          LiteralRAST("level"), 
          LiteralRAST("loud"), 
          LiteralRAST("low"), 
          LiteralRAST("lower"), 
          LiteralRAST("message-box"), 
          LiteralRAST("middle"), 
          LiteralRAST("mix"), 
          LiteralRAST("narrower"), 
          LiteralRAST("once"), 
          LiteralRAST("portrait"), 
          LiteralRAST("right-side"), 
          LiteralRAST("rightwards"), 
          LiteralRAST("silent"), 
          LiteralRAST("slow"), 
          LiteralRAST("slower"), 
          LiteralRAST("small-caption"), 
          LiteralRAST("soft"), 
          LiteralRAST("spell-out"), 
          LiteralRAST("status-bar"), 
          LiteralRAST("super"), 
          LiteralRAST("text-bottom"), 
          LiteralRAST("text-top"), 
          LiteralRAST("wider"), 
          LiteralRAST("x-fast"), 
          LiteralRAST("x-high"), 
          LiteralRAST("x-loud"), 
          LiteralRAST("x-low"), 
          LiteralRAST("x-soft"), 
          LiteralRAST("yes"), 
          LiteralRAST("pre"), 
          LiteralRAST("pre-wrap"), 
          LiteralRAST("pre-line")
        )), 
        "_functional_notation_keyword_values" -> ListRAST(List(
          LiteralRAST("attr"), 
          LiteralRAST("blackness"), 
          LiteralRAST("blend"), 
          LiteralRAST("blenda"), 
          LiteralRAST("blur"), 
          LiteralRAST("brightness"), 
          LiteralRAST("calc"), 
          LiteralRAST("circle"), 
          LiteralRAST("color-mod"), 
          LiteralRAST("contrast"), 
          LiteralRAST("counter"), 
          LiteralRAST("cubic-bezier"), 
          LiteralRAST("device-cmyk"), 
          LiteralRAST("drop-shadow"), 
          LiteralRAST("ellipse"), 
          LiteralRAST("gray"), 
          LiteralRAST("grayscale"), 
          LiteralRAST("hsl"), 
          LiteralRAST("hsla"), 
          LiteralRAST("hue"), 
          LiteralRAST("hue-rotate"), 
          LiteralRAST("hwb"), 
          LiteralRAST("image"), 
          LiteralRAST("inset"), 
          LiteralRAST("invert"), 
          LiteralRAST("lightness"), 
          LiteralRAST("linear-gradient"), 
          LiteralRAST("matrix"), 
          LiteralRAST("matrix3d"), 
          LiteralRAST("opacity"), 
          LiteralRAST("perspective"), 
          LiteralRAST("polygon"), 
          LiteralRAST("radial-gradient"), 
          LiteralRAST("rect"), 
          LiteralRAST("repeating-linear-gradient"), 
          LiteralRAST("repeating-radial-gradient"), 
          LiteralRAST("rgb"), 
          LiteralRAST("rgba"), 
          LiteralRAST("rotate"), 
          LiteralRAST("rotate3d"), 
          LiteralRAST("rotateX"), 
          LiteralRAST("rotateY"), 
          LiteralRAST("rotateZ"), 
          LiteralRAST("saturate"), 
          LiteralRAST("saturation"), 
          LiteralRAST("scale"), 
          LiteralRAST("scale3d"), 
          LiteralRAST("scaleX"), 
          LiteralRAST("scaleY"), 
          LiteralRAST("scaleZ"), 
          LiteralRAST("sepia"), 
          LiteralRAST("shade"), 
          LiteralRAST("skewX"), 
          LiteralRAST("skewY"), 
          LiteralRAST("steps"), 
          LiteralRAST("tint"), 
          LiteralRAST("toggle"), 
          LiteralRAST("translate"), 
          LiteralRAST("translate3d"), 
          LiteralRAST("translateX"), 
          LiteralRAST("translateY"), 
          LiteralRAST("translateZ"), 
          LiteralRAST("whiteness")
        )), 
        "_length_units" -> ListRAST(List(
          LiteralRAST("em"), 
          LiteralRAST("ex"), 
          LiteralRAST("ch"), 
          LiteralRAST("rem"), 
          LiteralRAST("vh"), 
          LiteralRAST("vw"), 
          LiteralRAST("vmin"), 
          LiteralRAST("vmax"), 
          LiteralRAST("px"), 
          LiteralRAST("mm"), 
          LiteralRAST("cm"), 
          LiteralRAST("in"), 
          LiteralRAST("pt"), 
          LiteralRAST("pc"), 
          LiteralRAST("q")
        )), 
        "_keyword_values" -> ListRAST(List(
          LiteralRAST("absolute"), 
          LiteralRAST("alias"), 
          LiteralRAST("all"), 
          LiteralRAST("all-petite-caps"), 
          LiteralRAST("all-scroll"), 
          LiteralRAST("all-small-caps"), 
          LiteralRAST("allow-end"), 
          LiteralRAST("alpha"), 
          LiteralRAST("alternate"), 
          LiteralRAST("alternate-reverse"), 
          LiteralRAST("always"), 
          LiteralRAST("armenian"), 
          LiteralRAST("auto"), 
          LiteralRAST("avoid"), 
          LiteralRAST("avoid-column"), 
          LiteralRAST("avoid-page"), 
          LiteralRAST("backwards"), 
          LiteralRAST("balance"), 
          LiteralRAST("baseline"), 
          LiteralRAST("below"), 
          LiteralRAST("blink"), 
          LiteralRAST("block"), 
          LiteralRAST("bold"), 
          LiteralRAST("bolder"), 
          LiteralRAST("border-box"), 
          LiteralRAST("both"), 
          LiteralRAST("bottom"), 
          LiteralRAST("box-decoration"), 
          LiteralRAST("break-word"), 
          LiteralRAST("capitalize"), 
          LiteralRAST("cell"), 
          LiteralRAST("center"), 
          LiteralRAST("circle"), 
          LiteralRAST("clip"), 
          LiteralRAST("clone"), 
          LiteralRAST("close-quote"), 
          LiteralRAST("col-resize"), 
          LiteralRAST("collapse"), 
          LiteralRAST("color"), 
          LiteralRAST("color-burn"), 
          LiteralRAST("color-dodge"), 
          LiteralRAST("column"), 
          LiteralRAST("column-reverse"), 
          LiteralRAST("compact"), 
          LiteralRAST("condensed"), 
          LiteralRAST("contain"), 
          LiteralRAST("container"), 
          LiteralRAST("content-box"), 
          LiteralRAST("context-menu"), 
          LiteralRAST("copy"), 
          LiteralRAST("cover"), 
          LiteralRAST("crisp-edges"), 
          LiteralRAST("crosshair"), 
          LiteralRAST("currentColor"), 
          LiteralRAST("cursive"), 
          LiteralRAST("darken"), 
          LiteralRAST("dashed"), 
          LiteralRAST("decimal"), 
          LiteralRAST("decimal-leading-zero"), 
          LiteralRAST("default"), 
          LiteralRAST("descendants"), 
          LiteralRAST("difference"), 
          LiteralRAST("digits"), 
          LiteralRAST("disc"), 
          LiteralRAST("distribute"), 
          LiteralRAST("dot"), 
          LiteralRAST("dotted"), 
          LiteralRAST("double"), 
          LiteralRAST("double-circle"), 
          LiteralRAST("e-resize"), 
          LiteralRAST("each-line"), 
          LiteralRAST("ease"), 
          LiteralRAST("ease-in"), 
          LiteralRAST("ease-in-out"), 
          LiteralRAST("ease-out"), 
          LiteralRAST("edges"), 
          LiteralRAST("ellipsis"), 
          LiteralRAST("end"), 
          LiteralRAST("ew-resize"), 
          LiteralRAST("exclusion"), 
          LiteralRAST("expanded"), 
          LiteralRAST("extra-condensed"), 
          LiteralRAST("extra-expanded"), 
          LiteralRAST("fantasy"), 
          LiteralRAST("fill"), 
          LiteralRAST("fill-box"), 
          LiteralRAST("filled"), 
          LiteralRAST("first"), 
          LiteralRAST("fixed"), 
          LiteralRAST("flat"), 
          LiteralRAST("flex"), 
          LiteralRAST("flex-end"), 
          LiteralRAST("flex-start"), 
          LiteralRAST("flip"), 
          LiteralRAST("force-end"), 
          LiteralRAST("forwards"), 
          LiteralRAST("from-image"), 
          LiteralRAST("full-width"), 
          LiteralRAST("geometricPrecision"), 
          LiteralRAST("georgian"), 
          LiteralRAST("groove"), 
          LiteralRAST("hanging"), 
          LiteralRAST("hard-light"), 
          LiteralRAST("help"), 
          LiteralRAST("hidden"), 
          LiteralRAST("hide"), 
          LiteralRAST("horizontal"), 
          LiteralRAST("hue"), 
          LiteralRAST("icon"), 
          LiteralRAST("infinite"), 
          LiteralRAST("inherit"), 
          LiteralRAST("initial"), 
          LiteralRAST("ink"), 
          LiteralRAST("inline"), 
          LiteralRAST("inline-block"), 
          LiteralRAST("inline-flex"), 
          LiteralRAST("inline-table"), 
          LiteralRAST("inset"), 
          LiteralRAST("inside"), 
          LiteralRAST("inter-word"), 
          LiteralRAST("invert"), 
          LiteralRAST("isolate"), 
          LiteralRAST("italic"), 
          LiteralRAST("justify"), 
          LiteralRAST("large"), 
          LiteralRAST("larger"), 
          LiteralRAST("last"), 
          LiteralRAST("left"), 
          LiteralRAST("lighten"), 
          LiteralRAST("lighter"), 
          LiteralRAST("line-through"), 
          LiteralRAST("linear"), 
          LiteralRAST("list-item"), 
          LiteralRAST("local"), 
          LiteralRAST("loose"), 
          LiteralRAST("lower-alpha"), 
          LiteralRAST("lower-greek"), 
          LiteralRAST("lower-latin"), 
          LiteralRAST("lower-roman"), 
          LiteralRAST("lowercase"), 
          LiteralRAST("ltr"), 
          LiteralRAST("luminance"), 
          LiteralRAST("luminosity"), 
          LiteralRAST("mandatory"), 
          LiteralRAST("manipulation"), 
          LiteralRAST("manual"), 
          LiteralRAST("margin-box"), 
          LiteralRAST("match-parent"), 
          LiteralRAST("medium"), 
          LiteralRAST("mixed"), 
          LiteralRAST("monospace"), 
          LiteralRAST("move"), 
          LiteralRAST("multiply"), 
          LiteralRAST("n-resize"), 
          LiteralRAST("ne-resize"), 
          LiteralRAST("nesw-resize"), 
          LiteralRAST("no-close-quote"), 
          LiteralRAST("no-drop"), 
          LiteralRAST("no-open-quote"), 
          LiteralRAST("no-repeat"), 
          LiteralRAST("none"), 
          LiteralRAST("normal"), 
          LiteralRAST("not-allowed"), 
          LiteralRAST("nowrap"), 
          LiteralRAST("ns-resize"), 
          LiteralRAST("nw-resize"), 
          LiteralRAST("nwse-resize"), 
          LiteralRAST("objects"), 
          LiteralRAST("oblique"), 
          LiteralRAST("off"), 
          LiteralRAST("on"), 
          LiteralRAST("open"), 
          LiteralRAST("open-quote"), 
          LiteralRAST("optimizeLegibility"), 
          LiteralRAST("optimizeSpeed"), 
          LiteralRAST("outset"), 
          LiteralRAST("outside"), 
          LiteralRAST("over"), 
          LiteralRAST("overlay"), 
          LiteralRAST("overline"), 
          LiteralRAST("padding-box"), 
          LiteralRAST("page"), 
          LiteralRAST("pan-down"), 
          LiteralRAST("pan-left"), 
          LiteralRAST("pan-right"), 
          LiteralRAST("pan-up"), 
          LiteralRAST("pan-x"), 
          LiteralRAST("pan-y"), 
          LiteralRAST("paused"), 
          LiteralRAST("petite-caps"), 
          LiteralRAST("pixelated"), 
          LiteralRAST("pointer"), 
          LiteralRAST("preserve-3d"), 
          LiteralRAST("progress"), 
          LiteralRAST("proximity"), 
          LiteralRAST("relative"), 
          LiteralRAST("repeat"), 
          LiteralRAST("repeat no-repeat"), 
          LiteralRAST("repeat-x"), 
          LiteralRAST("repeat-y"), 
          LiteralRAST("reverse"), 
          LiteralRAST("ridge"), 
          LiteralRAST("right"), 
          LiteralRAST("round"), 
          LiteralRAST("row"), 
          LiteralRAST("row-resize"), 
          LiteralRAST("row-reverse"), 
          LiteralRAST("rtl"), 
          LiteralRAST("ruby"), 
          LiteralRAST("ruby-base"), 
          LiteralRAST("ruby-base-container"), 
          LiteralRAST("ruby-text"), 
          LiteralRAST("ruby-text-container"), 
          LiteralRAST("run-in"), 
          LiteralRAST("running"), 
          LiteralRAST("s-resize"), 
          LiteralRAST("sans-serif"), 
          LiteralRAST("saturation"), 
          LiteralRAST("scale-down"), 
          LiteralRAST("screen"), 
          LiteralRAST("scroll"), 
          LiteralRAST("se-resize"), 
          LiteralRAST("semi-condensed"), 
          LiteralRAST("semi-expanded"), 
          LiteralRAST("separate"), 
          LiteralRAST("serif"), 
          LiteralRAST("sesame"), 
          LiteralRAST("show"), 
          LiteralRAST("sideways"), 
          LiteralRAST("sideways-left"), 
          LiteralRAST("sideways-right"), 
          LiteralRAST("slice"), 
          LiteralRAST("small"), 
          LiteralRAST("small-caps"), 
          LiteralRAST("smaller"), 
          LiteralRAST("smooth"), 
          LiteralRAST("snap"), 
          LiteralRAST("soft-light"), 
          LiteralRAST("solid"), 
          LiteralRAST("space"), 
          LiteralRAST("space-around"), 
          LiteralRAST("space-between"), 
          LiteralRAST("spaces"), 
          LiteralRAST("square"), 
          LiteralRAST("start"), 
          LiteralRAST("static"), 
          LiteralRAST("step-end"), 
          LiteralRAST("step-start"), 
          LiteralRAST("sticky"), 
          LiteralRAST("stretch"), 
          LiteralRAST("strict"), 
          LiteralRAST("stroke-box"), 
          LiteralRAST("style"), 
          LiteralRAST("sw-resize"), 
          LiteralRAST("table"), 
          LiteralRAST("table-caption"), 
          LiteralRAST("table-cell"), 
          LiteralRAST("table-column"), 
          LiteralRAST("table-column-group"), 
          LiteralRAST("table-footer-group"), 
          LiteralRAST("table-header-group"), 
          LiteralRAST("table-row"), 
          LiteralRAST("table-row-group"), 
          LiteralRAST("text"), 
          LiteralRAST("thick"), 
          LiteralRAST("thin"), 
          LiteralRAST("titling-caps"), 
          LiteralRAST("to"), 
          LiteralRAST("top"), 
          LiteralRAST("triangle"), 
          LiteralRAST("ultra-condensed"), 
          LiteralRAST("ultra-expanded"), 
          LiteralRAST("under"), 
          LiteralRAST("underline"), 
          LiteralRAST("unicase"), 
          LiteralRAST("unset"), 
          LiteralRAST("upper-alpha"), 
          LiteralRAST("upper-latin"), 
          LiteralRAST("upper-roman"), 
          LiteralRAST("uppercase"), 
          LiteralRAST("upright"), 
          LiteralRAST("use-glyph-orientation"), 
          LiteralRAST("vertical"), 
          LiteralRAST("vertical-text"), 
          LiteralRAST("view-box"), 
          LiteralRAST("visible"), 
          LiteralRAST("w-resize"), 
          LiteralRAST("wait"), 
          LiteralRAST("wavy"), 
          LiteralRAST("weight"), 
          LiteralRAST("weight style"), 
          LiteralRAST("wrap"), 
          LiteralRAST("wrap-reverse"), 
          LiteralRAST("x-large"), 
          LiteralRAST("x-small"), 
          LiteralRAST("xx-large"), 
          LiteralRAST("xx-small"), 
          LiteralRAST("zoom-in"), 
          LiteralRAST("zoom-out")
        )), 
        "_css_properties" -> ListRAST(List(
          LiteralRAST("align-content"), 
          LiteralRAST("align-items"), 
          LiteralRAST("align-self"), 
          LiteralRAST("alignment-baseline"), 
          LiteralRAST("all"), 
          LiteralRAST("animation"), 
          LiteralRAST("animation-delay"), 
          LiteralRAST("animation-direction"), 
          LiteralRAST("animation-duration"), 
          LiteralRAST("animation-fill-mode"), 
          LiteralRAST("animation-iteration-count"), 
          LiteralRAST("animation-name"), 
          LiteralRAST("animation-play-state"), 
          LiteralRAST("animation-timing-function"), 
          LiteralRAST("appearance"), 
          LiteralRAST("azimuth"), 
          LiteralRAST("backface-visibility"), 
          LiteralRAST("background"), 
          LiteralRAST("background-attachment"), 
          LiteralRAST("background-blend-mode"), 
          LiteralRAST("background-clip"), 
          LiteralRAST("background-color"), 
          LiteralRAST("background-image"), 
          LiteralRAST("background-origin"), 
          LiteralRAST("background-position"), 
          LiteralRAST("background-repeat"), 
          LiteralRAST("background-size"), 
          LiteralRAST("baseline-shift"), 
          LiteralRAST("bookmark-label"), 
          LiteralRAST("bookmark-level"), 
          LiteralRAST("bookmark-state"), 
          LiteralRAST("border"), 
          LiteralRAST("border-bottom"), 
          LiteralRAST("border-bottom-color"), 
          LiteralRAST("border-bottom-left-radius"), 
          LiteralRAST("border-bottom-right-radius"), 
          LiteralRAST("border-bottom-style"), 
          LiteralRAST("border-bottom-width"), 
          LiteralRAST("border-boundary"), 
          LiteralRAST("border-collapse"), 
          LiteralRAST("border-color"), 
          LiteralRAST("border-image"), 
          LiteralRAST("border-image-outset"), 
          LiteralRAST("border-image-repeat"), 
          LiteralRAST("border-image-slice"), 
          LiteralRAST("border-image-source"), 
          LiteralRAST("border-image-width"), 
          LiteralRAST("border-left"), 
          LiteralRAST("border-left-color"), 
          LiteralRAST("border-left-style"), 
          LiteralRAST("border-left-width"), 
          LiteralRAST("border-radius"), 
          LiteralRAST("border-right"), 
          LiteralRAST("border-right-color"), 
          LiteralRAST("border-right-style"), 
          LiteralRAST("border-right-width"), 
          LiteralRAST("border-spacing"), 
          LiteralRAST("border-style"), 
          LiteralRAST("border-top"), 
          LiteralRAST("border-top-color"), 
          LiteralRAST("border-top-left-radius"), 
          LiteralRAST("border-top-right-radius"), 
          LiteralRAST("border-top-style"), 
          LiteralRAST("border-top-width"), 
          LiteralRAST("border-width"), 
          LiteralRAST("bottom"), 
          LiteralRAST("box-decoration-break"), 
          LiteralRAST("box-shadow"), 
          LiteralRAST("box-sizing"), 
          LiteralRAST("box-snap"), 
          LiteralRAST("box-suppress"), 
          LiteralRAST("break-after"), 
          LiteralRAST("break-before"), 
          LiteralRAST("break-inside"), 
          LiteralRAST("caption-side"), 
          LiteralRAST("caret"), 
          LiteralRAST("caret-animation"), 
          LiteralRAST("caret-color"), 
          LiteralRAST("caret-shape"), 
          LiteralRAST("chains"), 
          LiteralRAST("clear"), 
          LiteralRAST("clip"), 
          LiteralRAST("clip-path"), 
          LiteralRAST("clip-rule"), 
          LiteralRAST("color"), 
          LiteralRAST("color-interpolation-filters"), 
          LiteralRAST("column-count"), 
          LiteralRAST("column-fill"), 
          LiteralRAST("column-gap"), 
          LiteralRAST("column-rule"), 
          LiteralRAST("column-rule-color"), 
          LiteralRAST("column-rule-style"), 
          LiteralRAST("column-rule-width"), 
          LiteralRAST("column-span"), 
          LiteralRAST("column-width"), 
          LiteralRAST("columns"), 
          LiteralRAST("content"), 
          LiteralRAST("counter-increment"), 
          LiteralRAST("counter-reset"), 
          LiteralRAST("counter-set"), 
          LiteralRAST("crop"), 
          LiteralRAST("cue"), 
          LiteralRAST("cue-after"), 
          LiteralRAST("cue-before"), 
          LiteralRAST("cursor"), 
          LiteralRAST("direction"), 
          LiteralRAST("display"), 
          LiteralRAST("dominant-baseline"), 
          LiteralRAST("elevation"), 
          LiteralRAST("empty-cells"), 
          LiteralRAST("filter"), 
          LiteralRAST("flex"), 
          LiteralRAST("flex-basis"), 
          LiteralRAST("flex-direction"), 
          LiteralRAST("flex-flow"), 
          LiteralRAST("flex-grow"), 
          LiteralRAST("flex-shrink"), 
          LiteralRAST("flex-wrap"), 
          LiteralRAST("float"), 
          LiteralRAST("float-defer"), 
          LiteralRAST("float-offset"), 
          LiteralRAST("float-reference"), 
          LiteralRAST("flood-color"), 
          LiteralRAST("flood-opacity"), 
          LiteralRAST("flow"), 
          LiteralRAST("flow-from"), 
          LiteralRAST("flow-into"), 
          LiteralRAST("font"), 
          LiteralRAST("font-family"), 
          LiteralRAST("font-feature-settings"), 
          LiteralRAST("font-kerning"), 
          LiteralRAST("font-language-override"), 
          LiteralRAST("font-size"), 
          LiteralRAST("font-size-adjust"), 
          LiteralRAST("font-stretch"), 
          LiteralRAST("font-style"), 
          LiteralRAST("font-synthesis"), 
          LiteralRAST("font-variant"), 
          LiteralRAST("font-variant-alternates"), 
          LiteralRAST("font-variant-caps"), 
          LiteralRAST("font-variant-east-asian"), 
          LiteralRAST("font-variant-ligatures"), 
          LiteralRAST("font-variant-numeric"), 
          LiteralRAST("font-variant-position"), 
          LiteralRAST("font-weight"), 
          LiteralRAST("footnote-display"), 
          LiteralRAST("footnote-policy"), 
          LiteralRAST("glyph-orientation-vertical"), 
          LiteralRAST("grid"), 
          LiteralRAST("grid-area"), 
          LiteralRAST("grid-auto-columns"), 
          LiteralRAST("grid-auto-flow"), 
          LiteralRAST("grid-auto-rows"), 
          LiteralRAST("grid-column"), 
          LiteralRAST("grid-column-end"), 
          LiteralRAST("grid-column-gap"), 
          LiteralRAST("grid-column-start"), 
          LiteralRAST("grid-gap"), 
          LiteralRAST("grid-row"), 
          LiteralRAST("grid-row-end"), 
          LiteralRAST("grid-row-gap"), 
          LiteralRAST("grid-row-start"), 
          LiteralRAST("grid-template"), 
          LiteralRAST("grid-template-areas"), 
          LiteralRAST("grid-template-columns"), 
          LiteralRAST("grid-template-rows"), 
          LiteralRAST("hanging-punctuation"), 
          LiteralRAST("height"), 
          LiteralRAST("hyphenate-character"), 
          LiteralRAST("hyphenate-limit-chars"), 
          LiteralRAST("hyphenate-limit-last"), 
          LiteralRAST("hyphenate-limit-lines"), 
          LiteralRAST("hyphenate-limit-zone"), 
          LiteralRAST("hyphens"), 
          LiteralRAST("image-orientation"), 
          LiteralRAST("image-resolution"), 
          LiteralRAST("initial-letter"), 
          LiteralRAST("initial-letter-align"), 
          LiteralRAST("initial-letter-wrap"), 
          LiteralRAST("isolation"), 
          LiteralRAST("justify-content"), 
          LiteralRAST("justify-items"), 
          LiteralRAST("justify-self"), 
          LiteralRAST("left"), 
          LiteralRAST("letter-spacing"), 
          LiteralRAST("lighting-color"), 
          LiteralRAST("line-break"), 
          LiteralRAST("line-grid"), 
          LiteralRAST("line-height"), 
          LiteralRAST("line-snap"), 
          LiteralRAST("list-style"), 
          LiteralRAST("list-style-image"), 
          LiteralRAST("list-style-position"), 
          LiteralRAST("list-style-type"), 
          LiteralRAST("margin"), 
          LiteralRAST("margin-bottom"), 
          LiteralRAST("margin-left"), 
          LiteralRAST("margin-right"), 
          LiteralRAST("margin-top"), 
          LiteralRAST("marker-side"), 
          LiteralRAST("marquee-direction"), 
          LiteralRAST("marquee-loop"), 
          LiteralRAST("marquee-speed"), 
          LiteralRAST("marquee-style"), 
          LiteralRAST("mask"), 
          LiteralRAST("mask-border"), 
          LiteralRAST("mask-border-mode"), 
          LiteralRAST("mask-border-outset"), 
          LiteralRAST("mask-border-repeat"), 
          LiteralRAST("mask-border-slice"), 
          LiteralRAST("mask-border-source"), 
          LiteralRAST("mask-border-width"), 
          LiteralRAST("mask-clip"), 
          LiteralRAST("mask-composite"), 
          LiteralRAST("mask-image"), 
          LiteralRAST("mask-mode"), 
          LiteralRAST("mask-origin"), 
          LiteralRAST("mask-position"), 
          LiteralRAST("mask-repeat"), 
          LiteralRAST("mask-size"), 
          LiteralRAST("mask-type"), 
          LiteralRAST("max-height"), 
          LiteralRAST("max-lines"), 
          LiteralRAST("max-width"), 
          LiteralRAST("min-height"), 
          LiteralRAST("min-width"), 
          LiteralRAST("mix-blend-mode"), 
          LiteralRAST("motion"), 
          LiteralRAST("motion-offset"), 
          LiteralRAST("motion-path"), 
          LiteralRAST("motion-rotation"), 
          LiteralRAST("move-to"), 
          LiteralRAST("nav-down"), 
          LiteralRAST("nav-left"), 
          LiteralRAST("nav-right"), 
          LiteralRAST("nav-up"), 
          LiteralRAST("object-fit"), 
          LiteralRAST("object-position"), 
          LiteralRAST("offset-after"), 
          LiteralRAST("offset-before"), 
          LiteralRAST("offset-end"), 
          LiteralRAST("offset-start"), 
          LiteralRAST("opacity"), 
          LiteralRAST("order"), 
          LiteralRAST("orphans"), 
          LiteralRAST("outline"), 
          LiteralRAST("outline-color"), 
          LiteralRAST("outline-offset"), 
          LiteralRAST("outline-style"), 
          LiteralRAST("outline-width"), 
          LiteralRAST("overflow"), 
          LiteralRAST("overflow-style"), 
          LiteralRAST("overflow-wrap"), 
          LiteralRAST("overflow-x"), 
          LiteralRAST("overflow-y"), 
          LiteralRAST("padding"), 
          LiteralRAST("padding-bottom"), 
          LiteralRAST("padding-left"), 
          LiteralRAST("padding-right"), 
          LiteralRAST("padding-top"), 
          LiteralRAST("page"), 
          LiteralRAST("page-break-after"), 
          LiteralRAST("page-break-before"), 
          LiteralRAST("page-break-inside"), 
          LiteralRAST("page-policy"), 
          LiteralRAST("pause"), 
          LiteralRAST("pause-after"), 
          LiteralRAST("pause-before"), 
          LiteralRAST("perspective"), 
          LiteralRAST("perspective-origin"), 
          LiteralRAST("pitch"), 
          LiteralRAST("pitch-range"), 
          LiteralRAST("play-during"), 
          LiteralRAST("polar-angle"), 
          LiteralRAST("polar-distance"), 
          LiteralRAST("position"), 
          LiteralRAST("presentation-level"), 
          LiteralRAST("quotes"), 
          LiteralRAST("region-fragment"), 
          LiteralRAST("resize"), 
          LiteralRAST("rest"), 
          LiteralRAST("rest-after"), 
          LiteralRAST("rest-before"), 
          LiteralRAST("richness"), 
          LiteralRAST("right"), 
          LiteralRAST("rotation"), 
          LiteralRAST("rotation-point"), 
          LiteralRAST("ruby-align"), 
          LiteralRAST("ruby-merge"), 
          LiteralRAST("ruby-position"), 
          LiteralRAST("running"), 
          LiteralRAST("scroll-snap-coordinate"), 
          LiteralRAST("scroll-snap-destination"), 
          LiteralRAST("scroll-snap-points-x"), 
          LiteralRAST("scroll-snap-points-y"), 
          LiteralRAST("scroll-snap-type"), 
          LiteralRAST("shape-image-threshold"), 
          LiteralRAST("shape-inside"), 
          LiteralRAST("shape-margin"), 
          LiteralRAST("shape-outside"), 
          LiteralRAST("size"), 
          LiteralRAST("speak"), 
          LiteralRAST("speak-as"), 
          LiteralRAST("speak-header"), 
          LiteralRAST("speak-numeral"), 
          LiteralRAST("speak-punctuation"), 
          LiteralRAST("speech-rate"), 
          LiteralRAST("stress"), 
          LiteralRAST("string-set"), 
          LiteralRAST("tab-size"), 
          LiteralRAST("table-layout"), 
          LiteralRAST("text-align"), 
          LiteralRAST("text-align-last"), 
          LiteralRAST("text-combine-upright"), 
          LiteralRAST("text-decoration"), 
          LiteralRAST("text-decoration-color"), 
          LiteralRAST("text-decoration-line"), 
          LiteralRAST("text-decoration-skip"), 
          LiteralRAST("text-decoration-style"), 
          LiteralRAST("text-emphasis"), 
          LiteralRAST("text-emphasis-color"), 
          LiteralRAST("text-emphasis-position"), 
          LiteralRAST("text-emphasis-style"), 
          LiteralRAST("text-indent"), 
          LiteralRAST("text-justify"), 
          LiteralRAST("text-orientation"), 
          LiteralRAST("text-overflow"), 
          LiteralRAST("text-shadow"), 
          LiteralRAST("text-space-collapse"), 
          LiteralRAST("text-space-trim"), 
          LiteralRAST("text-spacing"), 
          LiteralRAST("text-transform"), 
          LiteralRAST("text-underline-position"), 
          LiteralRAST("text-wrap"), 
          LiteralRAST("top"), 
          LiteralRAST("transform"), 
          LiteralRAST("transform-origin"), 
          LiteralRAST("transform-style"), 
          LiteralRAST("transition"), 
          LiteralRAST("transition-delay"), 
          LiteralRAST("transition-duration"), 
          LiteralRAST("transition-property"), 
          LiteralRAST("transition-timing-function"), 
          LiteralRAST("unicode-bidi"), 
          LiteralRAST("user-select"), 
          LiteralRAST("vertical-align"), 
          LiteralRAST("visibility"), 
          LiteralRAST("voice-balance"), 
          LiteralRAST("voice-duration"), 
          LiteralRAST("voice-family"), 
          LiteralRAST("voice-pitch"), 
          LiteralRAST("voice-range"), 
          LiteralRAST("voice-rate"), 
          LiteralRAST("voice-stress"), 
          LiteralRAST("voice-volume"), 
          LiteralRAST("volume"), 
          LiteralRAST("white-space"), 
          LiteralRAST("widows"), 
          LiteralRAST("width"), 
          LiteralRAST("will-change"), 
          LiteralRAST("word-break"), 
          LiteralRAST("word-spacing"), 
          LiteralRAST("word-wrap"), 
          LiteralRAST("wrap-after"), 
          LiteralRAST("wrap-before"), 
          LiteralRAST("wrap-flow"), 
          LiteralRAST("wrap-inside"), 
          LiteralRAST("wrap-through"), 
          LiteralRAST("writing-mode"), 
          LiteralRAST("z-index")
        ))
      )), 
      Includes(Map(
        "basics" -> List(
          MatchRule(
            name = None,
            regex = StaticRAST("/\\*(?:.|\\n)*?\\*/"),
            actions = List(
              Match(Token(
                clas = "Comment",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("\\{"),
            actions = List(
              Match(Token(
                clas = "Punctuation",
                template = "default"
              )), 
              Push("content")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(\\:{1,2})([\\w-]+)"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "Name.Decorator",
                  template = "default"
                )
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(\\.)([\\w-]+)"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "Name.Class",
                  template = "default"
                )
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(\\#)([\\w-]+)"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "Name.Namespace",
                  template = "default"
                )
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(@)([\\w-]+)"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "Keyword",
                  template = "default"
                )
              )), 
              Push("atrule")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[\\w-]+"),
            actions = List(
              Match(Token(
                clas = "Name.Tag",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[~^*!%&$\\[\\]()<>|+=@:;,./?-]"),
            actions = List(
              Match(Token(
                clas = "Operator",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("\"(\\\\\\\\|\\\\\"|[^\"])*\""),
            actions = List(
              Match(Token(
                clas = "String.Double",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("'(\\\\\\\\|\\\\'|[^'])*'"),
            actions = List(
              Match(Token(
                clas = "String.Single",
                template = "default"
              ))
            )
          )
        ), 
        "urls" -> List(
          MatchRule(
            name = None,
            regex = StaticRAST("(url)(\\()(\".*?\")(\\))"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Name.Builtin",
                  template = "default"
                ), 
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "String.Double",
                  template = "default"
                ), 
                Token(
                  clas = "Punctuation",
                  template = "default"
                )
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(url)(\\()('.*?')(\\))"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Name.Builtin",
                  template = "default"
                ), 
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "String.Single",
                  template = "default"
                ), 
                Token(
                  clas = "Punctuation",
                  template = "default"
                )
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(url)(\\()(.*?)(\\))"),
            actions = List(
              Groups(List(
                Token(
                  clas = "Name.Builtin",
                  template = "default"
                ), 
                Token(
                  clas = "Punctuation",
                  template = "default"
                ), 
                Token(
                  clas = "String.Other",
                  template = "default"
                ), 
                Token(
                  clas = "Punctuation",
                  template = "default"
                )
              ))
            )
          )
        ), 
        "numeric-values" -> List(
          MatchRule(
            name = None,
            regex = StaticRAST("\\#[a-zA-Z0-9]{1,6}"),
            actions = List(
              Match(Token(
                clas = "Number.Hex",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[+\\-]?[0-9]*[.][0-9]+"),
            actions = List(
              Match(Token(
                clas = "Number.Float",
                template = "default"
              )), 
              Push("numeric-end")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[+\\-]?[0-9]+"),
            actions = List(
              Match(Token(
                clas = "Number.Integer",
                template = "default"
              )), 
              Push("numeric-end")
            )
          )
        )
      )), 
      States(List(
        State(
          name = "root",
          rules = List(IncludeRule("basics"))
        ), 
        State(
          name = "atrule",
          rules = List(
            MatchRule(
              name = None,
              regex = StaticRAST("\\{"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Push("atcontent")
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST(";"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Pop
              )
            ), 
            IncludeRule("basics")
          )
        ), 
        State(
          name = "content",
          rules = List(
            MatchRule(
              name = None,
              regex = StaticRAST("\\}"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Pop
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST(";"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("^@.*?$"),
              actions = List(
                Match(Token(
                  clas = "Comment.Preproc",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = FunctionRAST(
                f = "words",
                args = List(
                  VariableRAST("_vendor_prefixes")
                )
              ),
              actions = List(
                Match(Token(
                  clas = "Keyword.Pseudo",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                StaticRAST("("), 
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_css_properties")
                  )
                ), 
                StaticRAST(")(\\s*)(\\:)")
              )),
              actions = List(
                Groups(List(
                  Token(
                    clas = "Keyword",
                    template = "default"
                  ), 
                  Token(
                    clas = "Text",
                    template = "default"
                  ), 
                  Token(
                    clas = "Punctuation",
                    template = "default"
                  )
                )), 
                Push("value-start")
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("([a-zA-Z_][\\w-]*)(\\s*)(\\:)"),
              actions = List(
                Groups(List(
                  Token(
                    clas = "Name",
                    template = "default"
                  ), 
                  Token(
                    clas = "Text",
                    template = "default"
                  ), 
                  Token(
                    clas = "Punctuation",
                    template = "default"
                  )
                )), 
                Push("value-start")
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("/\\*(?:.|\\n)*?\\*/"),
              actions = List(
                Match(Token(
                  clas = "Comment",
                  template = "default"
                ))
              )
            )
          )
        ), 
        State(
          name = "value-start",
          rules = List(
            MatchRule(
              name = None,
              regex = FunctionRAST(
                f = "words",
                args = List(
                  VariableRAST("_vendor_prefixes")
                )
              ),
              actions = List(
                Match(Token(
                  clas = "Name.Builtin.Pseudo",
                  template = "default"
                ))
              )
            ), 
            IncludeRule("urls"), 
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                StaticRAST("("), 
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_functional_notation_keyword_values")
                  )
                ), 
                StaticRAST(")(\\()")
              )),
              actions = List(
                Groups(List(
                  Token(
                    clas = "Name.Builtin",
                    template = "default"
                  ), 
                  Token(
                    clas = "Punctuation",
                    template = "default"
                  )
                )), 
                Push("function-start")
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("([a-zA-Z_][\\w-]+)(\\()"),
              actions = List(
                Groups(List(
                  Token(
                    clas = "Name.Function",
                    template = "default"
                  ), 
                  Token(
                    clas = "Punctuation",
                    template = "default"
                  )
                )), 
                Push("function-start")
              )
            ), 
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_keyword_values")
                  )
                ), 
                StaticRAST("\\b")
              )),
              actions = List(
                Match(Token(
                  clas = "Keyword.Constant",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_other_keyword_values")
                  )
                ), 
                StaticRAST("\\b")
              )),
              actions = List(
                Match(Token(
                  clas = "Keyword.Constant",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_color_keywords")
                  )
                ), 
                StaticRAST("\\b")
              )),
              actions = List(
                Match(Token(
                  clas = "Keyword.Constant",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_css_properties")
                  )
                ), 
                StaticRAST("\\b")
              )),
              actions = List(
                Match(Token(
                  clas = "Keyword",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\!important"),
              actions = List(
                Match(Token(
                  clas = "Comment.Preproc",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("/\\*(?:.|\\n)*?\\*/"),
              actions = List(
                Match(Token(
                  clas = "Comment",
                  template = "default"
                ))
              )
            ), 
            IncludeRule("numeric-values"), 
            MatchRule(
              name = None,
              regex = StaticRAST("[~^*!%&<>|+=@:./?-]+"),
              actions = List(
                Match(Token(
                  clas = "Operator",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("[\\[\\](),]+"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\"(\\\\\\\\|\\\\\"|[^\"])*\""),
              actions = List(
                Match(Token(
                  clas = "String.Double",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("'(\\\\\\\\|\\\\'|[^'])*'"),
              actions = List(
                Match(Token(
                  clas = "String.Single",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("[a-zA-Z_][\\w-]*"),
              actions = List(
                Match(Token(
                  clas = "Name",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST(";"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Pop
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\}"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Popn(2)
              )
            )
          )
        ), 
        State(
          name = "numeric-end",
          rules = List(
            MatchRule(
              name = None,
              regex = SeqRAST(List(
                FunctionRAST(
                  f = "words",
                  args = List(
                    VariableRAST("_all_units")
                  )
                ), 
                StaticRAST("\\b")
              )),
              actions = List(
                Match(Token(
                  clas = "Keyword.Type",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("%"),
              actions = List(
                Match(Token(
                  clas = "Keyword.Type",
                  template = "default"
                ))
              )
            ), 
            DefaultRule(List(Pop))
          )
        )
      ))
    ))

}
