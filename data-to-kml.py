import xml.etree.ElementTree as ET
import json

# Vars
NAME = "Transit Shapes"
DESC = "Shapes of Transit Routes"
COLOR_LINE = "7f00ffff"
COLOR_POLY = "7f00ff00"
WIDTH = "4"

FILENAME = "all-data-export.txt"
OUTPUT   = 'all-data.kml'


HEADER = '<?xml version="1.0" encoding="UTF-8"?>'
KML_ATTRB = {
    "xmlns"     : "http://www.opengis.net/kml/2.2",
    "xmlns:gx"  : "http://www.google.com/kml/ext/2.2",
    "xmlns:kml" : "http://www.opengis.net/kml/2.2",
    "xmlns:atom": "http://www.w3.org/2005/Atom"
}


# Funcs

def appendNew(tag, text, parent):
    element = ET.Element(tag)
    element.text = text
    parent.append(element)


def createStyle(line_color, poly_color, width, url):
    style = ET.Element('Style', attrib={'id' : url} )

    line_style = ET.Element('LineStyle')
    
    appendNew('color', line_color, line_style)
    appendNew('width', width, line_style)
    
    poly_style = ET.Element('PolyStyle')
    appendNew('color', poly_color, poly_style)
    
    style.append(line_style)
    style.append(poly_style)
    
    return style

def createPlacemark(name, style_url, coords):
    placemark = ET.Element('Placemark')
    appendNew('name', name, placemark)
    appendNew('styleUrl', style_url, placemark)

    line_string = ET.Element('LineString')
    appendNew('extrude', '1', line_string)
    appendNew('tessellate', '1', line_string)
    appendNew('altitudeMode', 'relativeToGround', line_string)
    appendNew('coordinates', coords, line_string)

    placemark.append(line_string)

    return placemark



# Main Code

root = ET.Element('kml', attrib=KML_ATTRB)
document = ET.Element('Document')
root.append(document)

appendNew('name', NAME, document)
appendNew('description', DESC, document)
document.append(createStyle(COLOR_LINE, COLOR_POLY, WIDTH, "#style_one"))

agencys = ['Island_Transit']

f = open(FILENAME, 'r').read()
data = json.loads(f)

for agency in data:
    if agency in agencys:
        print("Processing agency " + agency)

        for route in data[agency]:
            coords  = ""
            for point in data[agency][route]:
                coords += point[0] + "," + point[1] + " "

                
            placemark = createPlacemark(agency + "." + route, "#style_one", coords)
            document.append(placemark)

et = ET.ElementTree(root)
et.write(OUTPUT)



            

