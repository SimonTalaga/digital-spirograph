import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CycloidMultiDrawing extends PApplet {

// TODO : Bouton pour duppliquer une forme //<>// //<>//
// TODO : Bouton pour supprimer une forme


ControlP5 cp5;
Cycloid selected;

int margin = 20;
// Keeps track of the zposition of cycloids, incremented right after a cycloid is created
int indexCount = 0;
float rotationSpeed = 0.1f;

// UI Components
Group params; 
Bang delete, dupplicate, newC;
Slider r1, r2, ratio, turns, resolution, sw, zindex;
RadioButton b1;
Textfield t1, t2;
ColorPicker p1, p2, p3;

// Cycloids
ArrayList<Cycloid> cycloids = new ArrayList<Cycloid>();

public void setup() {
  

  Cycloid firstCycloid = new Cycloid(new PVector(width / 2, height / 2), 0.33f, 2, 100.0f, 100, 0.33f, true, indexCount);
  firstCycloid.setFillColour(color(random(0, 255), random(0, 255), random(0, 255)));
  firstCycloid.setStrokeColour(color(random(0, 255), random(0, 255), random(0, 255)));
  cycloids.add(firstCycloid);
  indexCount++;

  selected = cycloids.get(0);

  cp5 = new ControlP5(this);

  newC = cp5.addBang("bangNewCycloid")
    .setPosition(width - (margin / 2) - 30, margin / 2)
    .setSize(30, 30)
    .setTriggerEvent(Bang.RELEASE)
    .setLabel("New")
    .setColorLabel(color(0))
    ;

  params = cp5.addGroup("params")
    .setPosition(0, 10)
    .setSize(255  + margin, 560)
    .setBackgroundColor(color(100, 50));
    
  delete = cp5.addBang("bangDeleteCycloid")
    .setPosition(margin / 2, margin / 2) 
    .setSize(20, 20)
    .setTriggerEvent(Bang.RELEASE)
    .setLabel("DELETE")
    .setGroup(params)
    .setColorForeground(color(200, 0, 0))
    .setColorActive(color(255, 0, 0))
    ;  
  
  dupplicate = cp5.addBang("bangDupplicateCycloid")
    .setPosition(10 + delete.getWidth() + margin, margin / 2) 
    .setSize(20, 20)
    .setTriggerEvent(Bang.RELEASE)
    .setLabel("DUPPLICATE")
    .setGroup(params)
    .setColorForeground(color(0, 200, 0))
    .setColorActive(color(0, 255, 0))
    ;  

  b1 = cp5.addRadioButton("Type")
    .setPosition(10,  delete.getPosition()[1] + delete.getHeight() + 1.5f * margin)
    .setCaptionLabel("Type")
    .setWidth(20)
    .addItem("Epicycloid", 0)
    .addItem("Hypocycloid", 1)
    .setValue(0)
    .setGroup(params)
    .setItemsPerRow(2)
    .setSpacingColumn(50 + margin)
    .activate(selected.getType() == true ? 0 : 1)
    ;

  t1 = cp5.addTextfield("input")
    .setCaptionLabel("Numerateur")
    .setPosition(10, b1.getPosition()[1] + b1.getHeight() + (margin / 2))
    .setSize(50, 20) 
    .setFocus(true)
    .setColor(color(255))
    .setGroup(params)
    .show()
    ;

  t2 = cp5.addTextfield("input2")
    .setCaptionLabel("Denominateur")
    .setPosition(t1.getPosition()[0] + t1.getWidth() + margin / 2, b1.getPosition()[1] + b1.getHeight() + margin / 2)
    .setSize(50, 20) 
    .setFocus(true)
    .setColor(color(255))
    .setGroup(params)
    .show()
    ;

  cp5.addBang("bangratio")
    .setPosition(t2.getPosition()[0] + t2.getWidth() + margin / 2, b1.getPosition()[1] + b1.getHeight() + margin / 2)
    .setSize(20, 20)
    .setTriggerEvent(Bang.RELEASE)
    .setGroup(params)
    .setLabel("Compute ratio")
    ;   

  r1 = cp5.addSlider("r1")
    .setCaptionLabel("Size")
    .setPosition(10, t1.getPosition()[1] + t1.getHeight() + margin)
    .setSize(200, 20)
    .setRange(0, height / 2)
    .setValue(selected.getSize())
    .setColorCaptionLabel(color(20, 20, 20))
    .setGroup(params);

  r2 = cp5.addSlider("r2")
    .setCaptionLabel("Needles split")
    .setPosition(10, r1.getPosition()[1] + r1.getHeight() + (margin / 2))
    .setSize(200, 20)
    .setRange(0, 1)
    .setValue(selected.getNeedlesRatio())
    .setColorCaptionLabel(color(20, 20, 20))
    .setGroup(params);

  ratio = cp5.addSlider("ratio")
    .setCaptionLabel("Ratio")
    .setPosition(10, r2.getPosition()[1] + r2.getHeight() + (margin / 2))
    .setSize(200, 20)
    .setRange(0, 10)
    .setValue(selected.getRatio())
    .setColorCaptionLabel(color(20, 20, 20))
    .setGroup(params);     

  turns = cp5.addSlider("turns")
    .setCaptionLabel("Turns")
    .setPosition(10, ratio.getPosition()[1] + ratio.getHeight() + (margin / 2))
    .setSize(200, 20)
    .setRange(0, 20)
    .setValue(selected.getTurns())
    .setColorCaptionLabel(color(20, 20, 20))
    .setGroup(params);      

  resolution = cp5.addSlider("resolution")
    .setCaptionLabel("Resolution")
    .setPosition(10, turns.getPosition()[1] + turns.getHeight() + (margin / 2))
    .setSize(200, 20)
    .setRange(1, 100)
    .setValue(50)
    .setColorCaptionLabel(color(20, 20, 20))
    .setNumberOfTickMarks(100)
    .showTickMarks(false)
    .snapToTickMarks(true)
    .setGroup(params);    

  sw = cp5.addSlider("strokeweight")
    .setCaptionLabel("Stroke weight")
    .setPosition(10, resolution.getPosition()[1] + resolution.getHeight() + (margin / 2))
    .setSize(200, 20)
    .setRange(0, 50)
    .setValue(2)
    .setColorCaptionLabel(color(20, 20, 20))
    .setNumberOfTickMarks(51)
    .showTickMarks(true)
    .snapToTickMarks(true)
    .setGroup(params);    

  p1 = cp5.addColorPicker("Fill Colour")
    .setCaptionLabel("Fill Colour")
    .setPosition(10, sw.getPosition()[1] + sw.getHeight() + 1 * margin)
    .setWidth(width / 5) 
    .setLabel("Fill Colour")
    .setColorValue(selected.getFillColour())
    .setGroup(params)
    ;  

  p2 = cp5.addColorPicker("Stroke Colour")
    .setCaptionLabel("Stroke Colour")
    .setPosition(10, p1.getPosition()[1] + p1.getHeight() + 40 + margin)
    .setWidth(width / 5) 
    .setLabel("Fill Colour")
    .setColorValue(selected.getStrokeColour())
    .setGroup(params)
    ;

  p3 = cp5.addColorPicker("Background Colour")
    .setCaptionLabel("Background Colour")
    .setPosition(10, p2.getPosition()[1] + p2.getHeight() + 40 + margin)
    .setWidth(width / 5) 
    .setLabel("Background Colour")
    .setColorValue(color(255, 255, 255, 255))
    .setGroup(params)
    ;

  zindex = cp5.addSlider("zindex")
    .setTriggerEvent(Slider.RELEASE)
    .setCaptionLabel("z-index")
    .setPosition(10, p3.getPosition()[1] + p3.getHeight() + 40 + margin)
    .setSize(200, 20)
    .setColorCaptionLabel(color(20, 20, 20))
    .setGroup(params)
    .hide();
}

public void draw() {
  background(p3.getColorValue());

  if (indexCount > 1) {
    zindex.showTickMarks(true)
      .snapToTickMarks(true)
      .show();
  }

  for (int i = 0; i < indexCount; i++)
    for (int j = 0; i < cycloids.size(); j++)
      if (cycloids.get(j).getZindex() == i) {
        cycloids.get(j).drawShape();
        // To prevent bugs due to duplicated z-indexes
        break;
      }  

  if (selected != null) {
    selected.setTurns(turns.getValue());
    selected.setStrokeWeight(5);
    selected.setRatio(ratio.getValue());
    selected.setType(b1.getValue() == 0 ? true : false);
    selected.setStrokeColour(p2.getColorValue());
    selected.setFillColour(p1.getColorValue());
    selected.setSize(r1.getValue());
    selected.setNeedlesRatio(r2.getValue());
    selected.setResolution(resolution.getValue());
    selected.setStrokeWeight(PApplet.parseInt(sw.getValue()));
  }
}

public void mouseWheel(MouseEvent event) {
  if (selected != null) {
    float e = event.getCount();
    selected.setRotation(selected.getRotation() + rotationSpeed * e);
  }
}

public void mouseDragged() {
  // Check if we first clicked on the selected item
  if (selected != null && PVector.dist(selected.getPosition(), new PVector(mouseX, mouseY)) < selected.getSize())
    selected.setPosition(mouseX, mouseY);
}
public void mouseClicked() {

  if (mouseX > params.getWidth()) {
    
    if(mouseX < newC.getPosition()[0] || mouseX > newC.getPosition()[0] + newC.getWidth() && mouseY < newC.getPosition()[1] || mouseY > newC.getPosition()[1] + newC.getHeight()) {
      ArrayList<Integer> touches = new ArrayList<Integer>();
      for (int i = 0; i < cycloids.size(); i++) {
        if (PVector.dist(cycloids.get(i).getPosition(), new PVector(mouseX, mouseY)) < cycloids.get(i).getSize()) {
          touches.add(i);
        }
      }
  
      // If we clicked on a cycloid
      if (touches.size() > 0) {
        int indexOfHigher = touches.get(0);
        for (Integer touch : touches)
          if (cycloids.get(touch).getZindex() > cycloids.get(indexOfHigher).getZindex())
            indexOfHigher = touch;
      
        selected = cycloids.get(indexOfHigher);
      } 
      
      else {
        selected = null;
        params.hide();
      }
    }
    
    else if(mouseX > newC.getPosition()[0] && mouseX < newC.getPosition()[0] + newC.getWidth() && mouseY > newC.getPosition()[1] && mouseY < newC.getPosition()[1] + newC.getHeight()) {
      params.show();
    }
   
    if(selected != null) {
      // Update UI with selected values
      b1.activate(selected.getType() == true ? 0 : 1);   
      r1.setValue(selected.getSize());
      r2.setValue(selected.getNeedlesRatio());
      ratio.setValue(selected.getRatio());        
      turns.setValue(selected.getTurns());    
      p1.setColorValue(selected.getFillColour());    
      p2.setColorValue(selected.getStrokeColour());
      resolution.setValue(selected.getResolution());
      sw.setValue(selected.getStrokeWeight());
      zindex.setValue(selected.getZindex());
      params.show(); 
    }
    
    // If we don't click on the button to create a new cycloid, we unselect all //<>//
  }
}

public void updateZIndexBar() {
    if(cycloids.size() > 1) {       
      zindex.setNumberOfTickMarks(cycloids.size())
      .setRange(0, cycloids.size() - 1)
      .setValue(selected.getZindex())
      .show();
    } else {
      zindex.hide();
    }
}


public void controlEvent(ControlEvent theEvent) { 
  if (theEvent.getController().getName() == "bangratio") 
  {
    float rat = parseFloat(t1.getText()) / parseFloat(t2.getText());
    ratio.setValue(rat);
  } 
  
  else if (theEvent.getController().getName() == "bangNewCycloid") 
  {
    params.show();
    float num = PApplet.parseInt(random(1, 5));
    Cycloid newCycloid = new Cycloid(new PVector(random(params.getWidth(), width), random(0, height)), num / (num + PApplet.parseInt(random(0, 5))), 5, 30.0f, 50, random(0.2f, 0.8f), PApplet.parseInt(random(0, 2)) == 1 ? true : false, indexCount);
    newCycloid.setFillColour(color(random(0, 255), random(0, 255), random(0, 255)));
    newCycloid.setStrokeColour(color(random(0, 255), random(0, 255), random(0, 255)));
    cycloids.add(newCycloid);
    indexCount++;
    updateZIndexBar();
    selected = newCycloid;
  } 
  
  else if (theEvent.getController().getName() == "zindex") 
  {
    int newIndex = PApplet.parseInt(theEvent.getController().getValue());
    int previousIndex = selected.getZindex();
    int delta = newIndex - previousIndex;
    // On change premièrement le zIndex de la sélection
  
    // Décalage de tous les z-index entre previousIndex et newIndex
    int increment = delta / abs(delta);
    for (int i = previousIndex + increment; i != newIndex + increment; i += increment) {
      for (int j = 0; j < cycloids.size(); j++) {  
        Cycloid current = cycloids.get(j);
        if (current.getZindex() == i) {
          // Au cas où, pour éviter les doublons, on utilise la bonne valeur absolue, mais le mauvais signe
          current.setZindex((current.getZindex() + -increment) * -1);
        }
      }
    }

    selected.setZindex(newIndex);
    
    // On nettoie les valeurs négatives
    for (int i = 0; i < cycloids.size(); i++) {  
      cycloids.get(i).setZindex(abs(cycloids.get(i).getZindex()));
    }
  }
  
  else if (theEvent.getController().getName() == "bangDeleteCycloid") 
  {
    // Shift all the zindexes that are above)
    for(int i = selected.getZindex() + 1; i <= indexCount; i++) {
      for(int j = 0; j < cycloids.size(); j++) {
        Cycloid current = cycloids.get(j);
        if(current.getZindex() == i) {
          cycloids.get(j).setZindex(current.getZindex() - 1);
        }
      }
    }
    
    cycloids.remove(selected);
    selected = null;
    indexCount--;
    params.hide();
    
    updateZIndexBar();
  }
  
  else if (theEvent.getController().getName() == "bangDupplicateCycloid") 
  {
    Cycloid newCycloid = selected.clone(indexCount); 
    cycloids.add(newCycloid);
    indexCount++;
    updateZIndexBar();
    selected = newCycloid;
  }
}
class Cycloid {

  
  PVector center;
  // The current rotation of the cycloid
  float rotation;
  boolean type = true;
  // Ce ratio détermine la forme de la figure !
  float ratio = 3/5.0f;
  float resolution;
  // Nombre de tours de cercle qu'une ligne représente (par défaut 1)
  float turns = 6;
  // 
  float size;
  // This value (between 0 & 1) is the "position" of the separation between the two "needles" (or radius) used to draw the cycloids) 
  float needlesRatio;
  // Z-Index
  int zindex; 
  // Drawing attributes
  int fillColour, strokeColour;
  int strokeWeight;

  Cycloid(PVector center, float ratio, float turns, float resolution, float size, float needlesRatio, boolean type, int zindex) {
    this.center = center;
    this.ratio = ratio;
    this.turns = turns;
    this.size = size;
    this.needlesRatio = needlesRatio;
    this.resolution = resolution;
    this.rotation = 0;
    this.type = type;
    this.zindex = zindex;
    
    this.fillColour = color(0, 255, 255);
    this.strokeColour = color(255, 255, 0);
    this.strokeWeight = 3;
  }
  
  public Cycloid clone(int z) {
    Cycloid clone = new Cycloid(new PVector(this.center.x, this.center.y), this.ratio, this.turns, this.resolution, this.size, this.needlesRatio, this.type, z);
    clone.strokeColour =  this.strokeColour;
    clone.strokeWeight =  this.strokeWeight;
    clone.fillColour =  this.fillColour;
    
    return clone;
  }
  
  public void setFillColour(int colour) {
    this.fillColour = colour;
  }
  
  public boolean getType() {
    return this.type;
  }
  public int getFillColour() {
    return this.fillColour;
  }
  
  public int getStrokeColour() {
    return this.strokeColour;
  }
  
 public void setRatio(float ratio) {
    this.ratio = ratio;
 }
 
 public void setResolution(float resolution) {
   this.resolution = resolution;
 }
 
 public float getResolution() {
   return this.resolution;
 }
 
 public float getRatio() {
    return this.ratio;
 }
 
 public float getTurns() {
    return this.turns;
 }
 
  public void setTurns(float turns) {
    this.turns = turns;
  }
  
  public void setStrokeColour(int colour) {
    this.strokeColour = colour;
  }
  
  public void setStrokeWeight(int sw) {
    this.strokeWeight = sw;
  }
  
  public int getStrokeWeight() {
    return this.strokeWeight;
  }
  
  public void setPosition(int x, int y) {
    this.center = new PVector(x, y);
  }
 
  public int getZindex() {
    return this.zindex;
  }
  
  public void setZindex(int zindex) {
    this.zindex = zindex;
  }
  public PVector getPosition() {
    return this.center;
  }
  
 public void setNeedlesRatio(float ratio) {
   this.needlesRatio = ratio;
 }
 
 public float getNeedlesRatio() {
   return this.needlesRatio;
 }
 
 public void setSize(float size) {
   this.size = size;
 }
 
 public float getSize() {
   return this.size;
 }
  
  
  public void setRotation(float rotation) {
    this.rotation = rotation;
  }
  
  public float getRotation() {
    return this.rotation;
  }
  
  public void setType(boolean type) {
    this.type = type;
  }

  public void drawShape() {
    // Les deux longueurs utilisées (complexifie encore la figure) comme "rayons"
    float r1 = size * needlesRatio;
    float r2 = size * (1 - needlesRatio);
    
    //ellipse(center.x, center.y, 5, 5);

    // Resolution of the figure
    float step = TWO_PI / resolution;
    
    // Setup palette
    stroke(this.strokeColour); 
    if(this.fillColour == -1)
      noFill();
    else 
      fill(this.fillColour);
      
    if(this.strokeWeight == 0)
      noStroke();
    else
      strokeWeight(this.strokeWeight);
      
    beginShape();
    for (float i = 0; i < TWO_PI * turns; i += step) {
      float posX, posY;
      // Epicycloid
      if(type) { 
        // Rcos(a) - rcos(a + a / ratio)  
        posX = center.x - r1 * cos(rotation + i) - r2 * cos(rotation + i + i / ratio); 
        posY = center.y - r1 * sin(rotation + i) - r2 * sin(rotation + i + i / ratio);
      }
      // Hypocycloid
      else { 
        posX = center.x - r1 * cos(rotation + i) - r2 * cos(rotation + i - i / ratio); 
        posY = center.y - r1 * sin(rotation + i) - r2 * sin(rotation + i - i / ratio);
      }
      
      curveVertex(posX, posY);
    }
    endShape();
  }
}
  public void settings() {  size(800, 600, P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CycloidMultiDrawing" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
