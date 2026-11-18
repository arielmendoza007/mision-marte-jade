"""Convierte el reporte editable y los AUML simplificados en páginas legibles."""
from pathlib import Path
import html
import re

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, KeepTogether
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader
from pypdf import PdfReader, PdfWriter


RAIZ = Path(__file__).resolve().parent
if Path("C:/Windows/Fonts/arial.ttf").exists():
    regular, negrita = "C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/arialbd.ttf"
else:
    regular = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    negrita = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
pdfmetrics.registerFont(TTFont("ArialProyecto", regular))
pdfmetrics.registerFont(TTFont("ArialProyectoBold", negrita))
pdfmetrics.registerFontFamily("ArialProyecto", normal="ArialProyecto", bold="ArialProyectoBold",
    italic="ArialProyecto", boldItalic="ArialProyectoBold")
AZUL = colors.HexColor("#243D52")
NARANJA = colors.HexColor("#AF6336")
GRIS = colors.HexColor("#F2F4F4")


def limpiar(texto):
    texto = html.escape(texto)
    texto = re.sub(r"\[([^]]+)\]\(([^)]+)\)", r"\1 (\2)", texto)
    texto = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", texto)
    texto = re.sub(r"\*(.+?)\*", r"<i>\1</i>", texto)
    return texto.replace("`", "")


def reporte():
    normal = ParagraphStyle("cuerpo", fontName="ArialProyecto", fontSize=11,
        leading=15, spaceAfter=8, textColor=AZUL)
    titulo = ParagraphStyle("titulo", parent=normal, fontName="ArialProyectoBold",
        fontSize=17, leading=21, spaceAfter=12)
    seccion = ParagraphStyle("seccion", parent=normal, fontName="ArialProyectoBold",
        fontSize=14, leading=17, spaceBefore=12, spaceAfter=6, textColor=NARANJA)
    items = ParagraphStyle("item", parent=normal, leftIndent=14, firstLineIndent=-8)
    elementos = []
    lineas = (RAIZ / "Reporte-Olympus-Mons.md").read_text(encoding="utf-8").splitlines()
    import csv
    with (RAIZ.parent / "ensayo-local.csv").open(encoding="utf-8-sig") as archivo:
        resultados = list(csv.DictReader(archivo))
    if len(resultados) != 3:
        raise ValueError("Ejecuta el ensayo completo antes de imprimir")
    indice = 0
    while indice < len(lineas):
        linea = lineas[indice].strip()
        if not linea:
            indice += 1
            continue
        if linea == "[MEDICIONES]":
            for fila in resultados:
                resumen = (f"{fila['escenario']}. {fila['respuestas']}/{fila['propuestas']} respuestas. "
                    f"Latencia media {fila['latencia_media_us']} µs. Expedición {fila['tiempo_total_ms']} ms. "
                    f"Muestras entregadas {fila['muestras']}.")
                elementos.append(Paragraph(resumen, normal))
        elif linea.startswith("# "):
            elementos.append(Paragraph(limpiar(linea[2:]), titulo))
        elif linea.startswith("## "):
            elementos.append(Paragraph(limpiar(linea[3:]), seccion))
        elif linea.startswith("| "):
            tabla = []
            while indice < len(lineas) and lineas[indice].startswith("| "):
                celdas = [x.strip() for x in lineas[indice].strip(" |\n").split("|")]
                if not all(set(x) <= {"-", " "} for x in celdas):
                    tabla.append([Paragraph(limpiar(x), normal) for x in celdas])
                indice += 1
            t = Table(tabla, colWidths=[190, 330], repeatRows=1, hAlign="LEFT")
            t.setStyle(TableStyle([("GRID", (0,0),(-1,-1), .5, colors.HexColor("#AAB3B7")),
                ("BACKGROUND", (0,0),(-1,0), GRIS), ("VALIGN", (0,0),(-1,-1), "TOP"),
                ("LEFTPADDING", (0,0),(-1,-1), 8), ("RIGHTPADDING", (0,0),(-1,-1), 8)]))
            elementos.append(t)
            continue
        elif linea.startswith("- ") or re.match(r"\d+\. ", linea):
            elementos.append(Paragraph(limpiar(linea), items))
        else:
            parrafo = [linea]
            while indice + 1 < len(lineas) and lineas[indice + 1].strip() \
                    and not lineas[indice + 1].startswith(("#", "| ", "- ")):
                indice += 1
                parrafo.append(lineas[indice].strip())
            elementos.append(Paragraph(limpiar(" ".join(parrafo)), normal))
        indice += 1

    def pie(c, doc):
        c.setStrokeColor(colors.HexColor("#B6C1C7"))
        c.line(38, 38, A4[0] - 38, 38)
        c.setFont("ArialProyecto", 8)
        c.drawString(40, 26, "Expedición Olympus Mons  |  Sistemas Multiagentes")
        c.drawRightString(A4[0] - 40, 26, f"Página {doc.page}")

    SimpleDocTemplate(str(RAIZ / "Reporte-Olympus-Mons.pdf"), pagesize=A4,
        leftMargin=38, rightMargin=38, topMargin=40, bottomMargin=52,
        title="Expedición Olympus Mons - reporte").build(elementos,
        onFirstPage=pie, onLaterPages=pie)


PAGINA = landscape(A4)
W, H = PAGINA


def base(nombre, titulo, subtitulo):
    c = canvas.Canvas(str(RAIZ / nombre), pagesize=PAGINA)
    c.setTitle(titulo)
    c.setFillColor(AZUL)
    c.rect(0, H - 78, W, 78, fill=1, stroke=0)
    c.setFillColor(colors.white)
    c.setFont("ArialProyectoBold", 18)
    c.drawString(44, H - 41, titulo)
    c.setFont("ArialProyecto", 10)
    c.drawString(44, H - 60, subtitulo)
    c.setFillColor(AZUL)
    c.setFont("ArialProyecto", 8)
    c.drawString(44, 26, "Expedición Olympus Mons | Diagrama para imprimir en horizontal")
    anexos = ["Interfaz-local.pdf", "Arquitectura.pdf", "01-casos-de-uso.pdf", "02-clases.pdf", "03-interaccion.pdf"]
    pagina = len(PdfReader(str(RAIZ / "Reporte-Olympus-Mons.pdf")).pages) + anexos.index(nombre) + 1
    c.drawRightString(W - 44, 26, f"Página {pagina}  |  JADE / AUML")
    return c


def texto(c, x, y, contenido, tam=11, negrita=False):
    c.setFillColor(AZUL)
    c.setFont("ArialProyectoBold" if negrita else "ArialProyecto", tam)
    c.drawCentredString(x, y, contenido)


def linea(c, x1, y1, x2, y2, etiqueta="", dashed=False):
    import math
    c.setStrokeColor(AZUL)
    c.setFillColor(AZUL)
    c.setLineWidth(1.3)
    if dashed: c.setDash(4, 3)
    c.line(x1, y1, x2, y2)
    c.setDash()
    ang = math.atan2(y2 - y1, x2 - x1)
    size = 7
    p = c.beginPath()
    p.moveTo(x2, y2)
    p.lineTo(x2 - size * math.cos(ang - .5), y2 - size * math.sin(ang - .5))
    p.lineTo(x2 - size * math.cos(ang + .5), y2 - size * math.sin(ang + .5))
    p.close()
    c.drawPath(p, fill=1, stroke=0)
    if etiqueta:
        c.setFont("ArialProyecto", 11)
        c.drawCentredString((x1+x2)/2, (y1+y2)/2 + 8, etiqueta)


def caja(c, x, y, ancho, alto, titulo, detalles=()):
    c.setFillColor(GRIS)
    c.setStrokeColor(AZUL)
    c.roundRect(x, y, ancho, alto, 7, fill=1, stroke=1)
    texto(c, x + ancho/2, y + alto - 25, titulo, 13, True)
    for i, frase in enumerate(detalles):
        texto(c, x + ancho/2, y + alto - 47 - 17*i, frase, 11)


def casos():
    c = base("01-casos-de-uso.pdf", "2a. Casos de uso", "Actor: operador | Sistema: expedición autónoma")
    texto(c, 83, 340, "Operador", 12, True)
    c.setStrokeColor(AZUL)
    c.circle(83, 418, 15, stroke=1, fill=0)
    c.line(83, 403, 83, 362)
    c.line(54, 386, 112, 386)
    c.line(83, 362, 60, 340)
    c.line(83, 362, 106, 340)
    c.roundRect(195, 110, 600, 390, 8)
    texto(c, 495, 478, "Sistema: expedición Olympus Mons", 14, True)
    for y, nombre in [(395, "Iniciar expedición"), (290, "Consultar batería y muestras"),
                      (185, "Observar decisiones y resultado")]:
        c.setStrokeColor(AZUL)
        c.line(112, 387, 345, y)
        c.setFillColor(GRIS)
        c.ellipse(345, y-28, 665, y+28, fill=1, stroke=1)
        texto(c, 505, y-4, nombre, 13)
    c.showPage(); c.save()


def clases():
    c = base("02-clases.pdf", "2b. Clases y ontología", "Agentes JADE, sus comportamientos y objetos anidados (resumen legible)")
    columnas = [
        (48, "Entorno : Agent", ["EscucharEntorno (Cyclic)", "IniciarMision (OneShot)", "ConsultarRover (OneShot)", "VigilarMision (Waker)"]),
        (317, "Coordinador : Agent", ["EscucharCoordinador (Cyclic)", "EvaluarLectura (Simple)"]),
        (586, "Rover : Agent", ["PresentarseRover (OneShot)", "EscucharRover (Cyclic)", "TelemetriaRover (Ticker)", "EjecutarAccion (Waker)", "RegresarBase (Waker)"]),
    ]
    for x, agente, comportamientos in columnas:
        caja(c, x, 426, 206, 70, agente)
        caja(c, x, 252, 206, 152, "Comportamientos", comportamientos)
        linea(c, x + 103, 425, x + 103, 406)
    for x, titulo, tipo, ancho in [(44, "Observacion", "Predicate", 125),
                                  (209, "Propuesta", "Predicate", 125),
                                  (374, "Resultado", "Predicate", 125),
                                  (539, "Lectura", "Concept", 125),
                                  (704, "Sector", "Concept", 95)]:
        caja(c, x, 118, ancho, 76, titulo, [tipo])
    texto(c, W/2, 92, "Observacion, Propuesta y Resultado contienen Lectura; Lectura contiene Sector.", 11)
    texto(c, W/2, 72, "Otros predicados: Exploracion, EstadoRover y Metricas. Archivos editables: 02-clases.puml", 10)
    c.showPage(); c.save()

def interaccion():
    c = base("03-interaccion.pdf", "2c. Interacción AUML", "Mismo conversation-id M-n en todo el ciclo; los trazos horizontales son mensajes ACL")
    xs = [84, 278, 470, 665]
    nombres = ["Operador", "Entorno", "Rover", "Coordinador"]
    for x, n in zip(xs, nombres):
        caja(c, x-70, 459, 140, 43, n)
        c.setStrokeColor(colors.HexColor("#A1AEB5"))
        c.setDash(3, 3)
        c.line(x, 458, x, 77)
        c.setDash()
    mensajes = [
        (84,278,"inicia escenario",440),
        (278,470,"REQUEST Exploracion",415),
        (470,278,"QUERY_REF Exploracion",390),
        (278,470,"INFORM Observacion",365),
        (470,665,"REQUEST Observacion",340),
        (665,470,"PROPOSE Propuesta",315),
        (665,278,"INFORM Propuesta",290),
        (470,665,"Acepta o rechaza propuesta",265),
        (470,278,"INFORM acción en curso",240),
        (470,278,"INFORM regreso en curso",215),
        (470,278,"INFORM Resultado final al regresar",190),
        (278,665,"INFORM Resultado final",165),
        (665,278,"INFORM Metricas",140),
        (278,84,"muestra resultado y medidas",115),
    ]
    for a,b,label,y in mensajes:
        c.setStrokeColor(AZUL)
        c.setLineWidth(1)
        c.line(a,y,b-8 if b>a else b+8,y)
        linea(c, b-8 if b>a else b+8,y,b,y)
        c.setFillColor(AZUL)
        c.setFont("ArialProyecto", 11)
        c.drawCentredString((a+b)/2, y+5, label)
    texto(c, W/2, 69, "Las lecturas y las propuestas se repiten por tramo; tras la muestra o un rechazo, el Rover regresa.", 10)
    c.showPage(); c.save()


def arquitectura():
    c = base("Arquitectura.pdf", "1c. Arquitectura distribuida", "Dos contenedores de una plataforma JADE | Ontología y codec compartidos")
    c.setFillColor(GRIS)
    c.roundRect(44, 170, 494, 330, 10, stroke=1, fill=1)
    c.roundRect(588, 170, 207, 330, 10, stroke=1, fill=1)
    texto(c, 291, 472, "Computadora principal / contenedor Main", 13, True)
    texto(c, 691, 472, "Segunda laptop", 13, True)
    caja(c, 78, 324, 178, 110, "Entorno", ["Interfaz Swing", "Sensores simulados"])
    caja(c, 322, 324, 178, 110, "Coordinador", ["Reglas R1-R5", "Métricas ACL"])
    caja(c, 603, 324, 178, 110, "Rover", ["Energía y posición", "Decisión y ejecución"])
    linea(c, 258, 370, 315, 370, "INFORM")
    linea(c, 500, 390, 596, 390, "PROPOSE")
    linea(c, 607, 348, 506, 348, "REQUEST")
    linea(c, 603, 272, 262, 272, "QUERY_REF / INFORM")
    texto(c, 420, 210, "JADE 4.6.0 | MisionMarte (BeanOntology) | FIPA-SL | red local o Hamachi", 11)
    c.showPage(); c.save()


def captura():
    c = base("Interfaz-local.pdf", "3. Interfaz en el ensayo local",
             "Caso BATERIA: regreso preventivo; captura generada por EnsayoConsola")
    imagen = ImageReader(str(RAIZ.parent / "captura-consola-local.png"))
    ancho, alto = imagen.getSize()
    limite_w, limite_h = W - 88, H - 172
    escala = min(limite_w/ancho, limite_h/alto)
    ancho, alto = ancho*escala, alto*escala
    c.drawImage(imagen, (W-ancho)/2, 80, ancho, alto)
    c.setFont("ArialProyecto", 9)
    c.setFillColor(AZUL)
    c.drawString(44, 62, "Captura local: no constituye evidencia de comunicación entre dos laptops.")
    c.showPage(); c.save()


def entrega():
    escritor = PdfWriter()
    for nombre in ["Reporte-Olympus-Mons.pdf", "Interfaz-local.pdf", "Arquitectura.pdf", "01-casos-de-uso.pdf",
                   "02-clases.pdf", "03-interaccion.pdf"]:
        for pagina in PdfReader(str(RAIZ / nombre)).pages:
            escritor.add_page(pagina)
    with (RAIZ / "Entrega-imprimible.pdf").open("wb") as archivo:
        escritor.write(archivo)


if __name__ == "__main__":
    reporte()
    captura()
    arquitectura()
    casos()
    clases()
    interaccion()
    entrega()
    print("PDF imprimibles y diagramas generados en", RAIZ)
