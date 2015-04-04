/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
Part of the Processing project - http://processing.org
Copyright (c) 2012-15 The Processing Foundation

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License version 2
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software Foundation, Inc.
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/

package processing.mode.java.pdex;

import processing.mode.java.JavaMode;
import processing.mode.java.JavaEditor;
import processing.mode.java.tweak.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

import processing.app.Base;
import processing.app.Editor;
import processing.app.SketchCode;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TextAreaDefaults;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TokenMarker;


/**
 * Customized line painter. Adds support for background colors,
 * left hand gutter area with background color and text.
 * TODO Most of this needs to be merged into the main TextAreaPainter,
 * since it has nothing to do with Java. [fry]
 */
public class JavaTextAreaPainter extends TextAreaPainter
	implements MouseListener, MouseMotionListener {

//  protected JavaTextArea ta; // we need the subclassed textarea
  protected ErrorCheckerService errorCheckerService;

  public Color errorColor; // = new Color(0xED2630);
  public Color warningColor; // = new Color(0xFFC30E);
  public Color errorMarkerColor; // = new Color(0xED2630);
  public Color warningMarkerColor; // = new Color(0xFFC30E);

  protected Font gutterTextFont;
  protected Color gutterTextColor;
//  protected Color gutterTempColor;

  public static class ErrorLineCoord {
	public int xStart;
	public int xEnd;
	public int yStart;
	public int yEnd;
	public Problem problem;

	public ErrorLineCoord(int xStart, int xEnd, int yStart, int yEnd, Problem problem) {
	  this.xStart = xStart;
	  this.xEnd = xEnd;
	  this.yStart = yStart;
	  this.yEnd = yEnd;
	  this.problem = problem;
    }
  }
  public List<ErrorLineCoord> errorLineCoords = new ArrayList<>();

//  static int ctrlMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();


  public JavaTextAreaPainter(JavaTextArea textArea, TextAreaDefaults defaults) {
    super(textArea, defaults);

    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if (!getEditor().hasJavaTabs()) { // Ctrl + Click disabled for java tabs
          if (evt.getButton() == MouseEvent.BUTTON1) {
            if (evt.isControlDown() || evt.isMetaDown()) {
              handleCtrlClick(evt);
            }
          }
        }
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(final MouseEvent evt) {
    	for (ErrorLineCoord coord : errorLineCoords) {
    	  if (evt.getX() >= coord.xStart && evt.getX() <= coord.xEnd
    			  && evt.getY() >= coord.yStart && evt.getY() <= coord.yEnd + 2) {
    	    setToolTipText(coord.problem.getMessage());
    		break;
    	  }
    	}
      }
	});

    // TweakMode code
    interactiveMode = false;
    cursorType = Cursor.DEFAULT_CURSOR;
  }


  void handleCtrlClick(MouseEvent evt) {
    Base.log("--handleCtrlClick--");
    int off = textArea.xyToOffset(evt.getX(), evt.getY());
    if (off < 0)
      return;
    int line = textArea.getLineOfOffset(off);
    if (line < 0)
      return;
    String s = textArea.getLineText(line);
    if (s == null)
      return;
    else if (s.length() == 0)
      return;
    else {
      int x = textArea.xToOffset(line, evt.getX()), x2 = x + 1, x1 = x - 1;
      Base.log("x="+x);
      int xLS = off - textArea.getLineStartNonWhiteSpaceOffset(line);
      if (x < 0 || x >= s.length())
        return;
      String word = s.charAt(x) + "";
      if (s.charAt(x) == ' ')
        return;
      if (!(Character.isLetterOrDigit(s.charAt(x)) || s.charAt(x) == '_' || s.charAt(x) == '$'))
        return;
      int i = 0;
      while (true) {
        i++;
        if (x1 >= 0 && x1 < s.length()) {
          if (Character.isLetter(s.charAt(x1)) || s.charAt(x1) == '_') {
            word = s.charAt(x1--) + word;
            xLS--;
          } else
            x1 = -1;
        } else
          x1 = -1;

        if (x2 >= 0 && x2 < s.length()) {
          if (Character.isLetterOrDigit(s.charAt(x2)) || s.charAt(x2) == '_'
              || s.charAt(x2) == '$')
            word = word + s.charAt(x2++);
          else
            x2 = -1;
        } else
          x2 = -1;

        if (x1 < 0 && x2 < 0)
          break;
        if (i > 200) {
          // time out!
          // System.err.println("Whoopsy! :P");
          break;
        }
      }
      if (Character.isDigit(word.charAt(0)))
        return;

      Base.log(errorCheckerService.mainClassOffset + line + "|" + line + "| offset " + xLS + word + " <= \n");
      errorCheckerService.getASTGenerator().scrollToDeclaration(line, word, xLS);
    }
  }


//  private void loadTheme(ExperimentalMode mode) {
//    errorColor = mode.getThemeColor("editor.errorcolor", errorColor);
//    warningColor = mode.getThemeColor("editor.warningcolor", warningColor);
//    errorMarkerColor = mode.getThemeColor("editor.errormarkercolor", errorMarkerColor);
//    warningMarkerColor = mode.getThemeColor("editor.warningmarkercolor", warningMarkerColor);
//  }


  /**
   * Paint a line. Paints the gutter (with background color and text) then the
   * line (background color and text).
   *
   * @param gfx
   *          the graphics context
   * @param tokenMarker
   * @param line
   *          0-based line number
   * @param x
   *          horizontal position
   */
  @Override
  protected void paintLine(Graphics gfx, int line, int x,
                           TokenMarker tokenMarker) {
    try {
      // TODO This line is causing NPEs randomly ever since I added the
      // toggle for Java Mode/Debugger toolbar. [Manindra]
      super.paintLine(gfx, line, x + Editor.LEFT_GUTTER, tokenMarker);

    } catch (Exception e) {
      Base.log(e.getMessage());
    }

    // formerly only when in debug mode
    paintLeftGutter(gfx, line, x);
//    paintGutterBg(gfx, line, x);
//    paintGutterLine(gfx, line, x);
//    paintGutterText(gfx, line, x);

    paintErrorLine(gfx, line, x);
  }


  /**
   * Paint the gutter: draw the background, draw line numbers, break points.
   * @param gfx the graphics context
   * @param line 0-based line number
   * @param x horizontal position
   */
  protected void paintLeftGutter(Graphics gfx, int line, int x) {
    gfx.setColor(getTextArea().gutterBgColor);
//    gfx.setColor(Color.ORANGE);
    int y = textArea.lineToY(line) + fm.getLeading() + fm.getMaxDescent();
    gfx.fillRect(0, y, Editor.LEFT_GUTTER, fm.getHeight());

    String text = getTextArea().getGutterText(line);
    // if no special text for a breakpoint, just show the line number
    if (text == null) {
      text = String.valueOf(line + 1);
    }
    char[] txt = text.toCharArray();

    //gfx.setFont(getFont());
    gfx.setFont(gutterTextFont);
    // Right-align the text
    int tx = Editor.LEFT_GUTTER - Editor.GUTTER_MARGIN -
      gfx.getFontMetrics().charsWidth(txt, 0, txt.length);
    gfx.setColor(gutterTextColor);
    // Using 'fm' here because it's relative to the editor text size,
    // not the numbers in the gutter
    int ty = textArea.lineToY(line) + fm.getHeight();
    Utilities.drawTabbedText(new Segment(txt, 0, text.length()),
                             tx, ty, gfx, this, 0);
  }


//  /**
//   * Paint the gutter background (solid color).
//   *
//   * @param gfx
//   *          the graphics context
//   * @param line
//   *          0-based line number
//   * @param x
//   *          horizontal position
//   */
//  protected void paintGutterBg(Graphics gfx, int line, int x) {
//    gfx.setColor(getTextArea().gutterBgColor);
//    gfx.setColor(Color.ORANGE);
//    int y = textArea.lineToY(line) + fm.getLeading() + fm.getMaxDescent();
//    gfx.fillRect(0, y, Editor.LEFT_GUTTER, fm.getHeight());
//  }


//  /**
//   * Paint the vertical gutter separator line.
//   *
//   * @param gfx
//   *          the graphics context
//   * @param line
//   *          0-based line number
//   * @param x
//   *          horizontal position
//   */
//  protected void paintGutterLine(Graphics gfx, int line, int x) {
//    int y = textArea.lineToY(line) + fm.getLeading() + fm.getMaxDescent();
//    gfx.setColor(getTextArea().gutterLineColor);
//    gfx.setColor(Color.GREEN);
//    gfx.drawLine(Editor.LEFT_GUTTER, y,
//                 Editor.LEFT_GUTTER, y + fm.getHeight());
//  }


//  /**
//   * Paint the gutter text.
//   *
//   * @param gfx
//   *          the graphics context
//   * @param line
//   *          0-based line number
//   * @param x
//   *          horizontal position
//   */
//  protected void paintGutterText(Graphics gfx, int line, int x) {
//    String text = getTextArea().getGutterText(line);
//    if (text == null) {
//      return;
//    }
//
//    gfx.setFont(getFont());
//    Color textColor = getTextArea().getGutterTextColor(line);
//    if (textColor == null) {
//      gfx.setColor(getForeground());
//    } else {
//      gfx.setColor(textColor);
//    }
//    int y = textArea.lineToY(line) + fm.getHeight();
//
//    // draw 4 times to make it appear bold, displaced 1px to the right, to the bottom and bottom right.
//    //int len = text.length() > ta.gutterChars ? ta.gutterChars : text.length();
//    Utilities.drawTabbedText(new Segment(text.toCharArray(), 0, text.length()),
//                             Editor.GUTTER_MARGIN, y, gfx, this, 0);
//    Utilities.drawTabbedText(new Segment(text.toCharArray(), 0, text.length()),
//                             Editor.GUTTER_MARGIN + 1, y, gfx, this, 0);
//    Utilities.drawTabbedText(new Segment(text.toCharArray(), 0, text.length()),
//                             Editor.GUTTER_MARGIN, y + 1, gfx, this, 0);
//    Utilities.drawTabbedText(new Segment(text.toCharArray(), 0, text.length()),
//                             Editor.GUTTER_MARGIN + 1, y + 1, gfx, this, 0);
//  }


  /**
   * Paint the background color of a line.
   *
   * @param gfx
   *          the graphics context
   * @param line
   *          0-based line number
   * @param x
   */
  protected void paintLineBgColor(Graphics gfx, int line, int x) {
    int y = textArea.lineToY(line);
    y += fm.getLeading() + fm.getMaxDescent();
    int height = fm.getHeight();

    // get the color
    Color col = getTextArea().getLineBgColor(line);
    //System.out.print("bg line " + line + ": ");
    // no need to paint anything
    if (col == null) {
      //log("none");
      return;
    }
    // paint line background
    gfx.setColor(col);
    gfx.fillRect(0, y, getWidth(), height);
  }


  /**
   * Paints the underline for an error/warning line
   *
   * @param gfx
   *          the graphics context
   * @param tokenMarker
   * @param line
   *          0-based line number: NOTE
   * @param x
   */
  protected void paintErrorLine(Graphics gfx, int line, int x) {
    if (errorCheckerService == null) {
      return;
    }

    if (errorCheckerService.problemsList == null) {
      return;
    }

    boolean notFound = true;
    boolean isWarning = false;
    Problem problem = null;

    errorLineCoords.clear();
    // Check if current line contains an error. If it does, find if it's an
    // error or warning
    for (ErrorMarker emarker : errorCheckerService.getEditor().getErrorPoints()) {
      if (emarker.getProblem().getLineNumber() == line) {
        notFound = false;
        if (emarker.getType() == ErrorMarker.Warning) {
          isWarning = true;
        }
        problem = emarker.getProblem();
        //log(problem.toString());
        break;
      }
    }

    if (notFound) {
      return;
    }

    // Determine co-ordinates
    // log("Hoff " + ta.getHorizontalOffset() + ", " +
    // horizontalAdjustment);
    int y = textArea.lineToY(line);
    y += fm.getLeading() + fm.getMaxDescent();
//    int height = fm.getHeight();
    int start = textArea.getLineStartOffset(line) + problem.getPDELineStartOffset();
    int pLength = problem.getPDELineStopOffset() + 1 - problem.getPDELineStartOffset();

    try {
      String badCode = null;
      String goodCode = null;
      try {
        SyntaxDocument doc = textArea.getDocument();
        badCode = doc.getText(start, pLength);
        goodCode = doc.getText(textArea.getLineStartOffset(line), problem.getPDELineStartOffset());
        //log("paintErrorLine() LineText GC: " + goodCode);
        //log("paintErrorLine() LineText BC: " + badCode);
      } catch (BadLocationException bl) {
        // Error in the import statements or end of code.
        // System.out.print("BL caught. " + ta.getLineCount() + " ,"
        // + line + " ,");
        // log((ta.getLineStopOffset(line) - start - 1));
        return;
      }

      // Take care of offsets
      int aw = fm.stringWidth(trimRight(badCode)) + textArea.getHorizontalOffset(); // apparent width. Whitespaces
      // to the left of line + text
      // width
      int rw = fm.stringWidth(badCode.trim()); // real width
      int x1 = fm.stringWidth(goodCode) + (aw - rw), y1 = y + fm.getHeight()
          - 2, x2 = x1 + rw;
      // Adding offsets for the gutter
      x1 += Editor.LEFT_GUTTER;
      x2 += Editor.LEFT_GUTTER;

      errorLineCoords.add(new ErrorLineCoord(x1,  x2, y, y1, problem));

      // gfx.fillRect(x1, y, rw, height);

      // Let the painting begin!

      // Little rect at starting of a line containing errors - disabling it for now
//      gfx.setColor(errorMarkerColor);
//      if (isWarning) {
//        gfx.setColor(warningMarkerColor);
//      }
//      gfx.fillRect(1, y + 2, 3, height - 2);

      gfx.setColor(errorColor);
      if (isWarning) {
        gfx.setColor(warningColor);
      }
      int xx = x1;

      // Draw the jagged lines
      while (xx < x2) {
        gfx.drawLine(xx, y1, xx + 2, y1 + 1);
        xx += 2;
        gfx.drawLine(xx, y1 + 1, xx + 2, y1);
        xx += 2;
      }
    } catch (Exception e) {
      System.out
          .println("Looks like I messed up! XQTextAreaPainter.paintLine() : "
              + e);
      //e.printStackTrace();
    }

    // Won't highlight the line. Select the text instead.
    // gfx.setColor(Color.RED);
    // gfx.fillRect(2, y, 3, height);
  }


  /**
   * Trims out trailing whitespaces (to the right)
   *
   * @param string
   * @return - String
   */
  static private String trimRight(String string) {
    String newString = "";
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) != ' ') {
        newString = string.substring(0, i) + string.trim();
        break;
      }
    }
    return newString;
  }


  /**
   * Sets ErrorCheckerService and loads theme for TextAreaPainter(XQMode)
   *
   * @param ecs
   * @param mode
   */
  public void setECSandTheme(ErrorCheckerService ecs, JavaMode mode) {
    this.errorCheckerService = ecs;
    //loadTheme(mode);

    errorColor = mode.getColor("editor.errorcolor"); //, errorColor);
    warningColor = mode.getColor("editor.warningcolor"); //, warningColor);
    errorMarkerColor = mode.getColor("editor.errormarkercolor"); //, errorMarkerColor);
    warningMarkerColor = mode.getColor("editor.warningmarkercolor"); //, warningMarkerColor);

    gutterTextFont = mode.getFont("editor.gutter.text.font");
    gutterTextColor = mode.getColor("editor.gutter.text.color");
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    if (!getEditor().hasJavaTabs()) {
      int off = textArea.xyToOffset(event.getX(), event.getY());
      if (off < 0) {
        setToolTipText(null);
        return super.getToolTipText(event);
      }
      int line = textArea.getLineOfOffset(off);
      if (line < 0) {
        setToolTipText(null);
        return super.getToolTipText(event);
      }
      String s = textArea.getLineText(line);
      if (s == "") {
        return event.toString();

      } else if (s.length() == 0) {
        setToolTipText(null);
        return super.getToolTipText(event);

      } else {
        int x = textArea.xToOffset(line, event.getX()), x2 = x + 1, x1 = x - 1;
        int xLS = off - textArea.getLineStartNonWhiteSpaceOffset(line);
        if (x < 0 || x >= s.length()) {
          setToolTipText(null);
          return super.getToolTipText(event);
        }
        String word = s.charAt(x) + "";
        if (s.charAt(x) == ' ') {
          setToolTipText(null);
          return super.getToolTipText(event);
        }
        if (!(Character.isLetterOrDigit(s.charAt(x)) ||
            s.charAt(x) == '_' || s.charAt(x) == '$')) {
          setToolTipText(null);
          return super.getToolTipText(event);
        }
        int i = 0;
        while (true) {
          i++;
          if (x1 >= 0 && x1 < s.length()) {
            if (Character.isLetter(s.charAt(x1)) || s.charAt(x1) == '_') {
              word = s.charAt(x1--) + word;
              xLS--;
            } else
              x1 = -1;
          } else
            x1 = -1;

          if (x2 >= 0 && x2 < s.length()) {
            if (Character.isLetterOrDigit(s.charAt(x2)) || s.charAt(x2) == '_'
                || s.charAt(x2) == '$')
              word = word + s.charAt(x2++);
            else
              x2 = -1;
          } else
            x2 = -1;

          if (x1 < 0 && x2 < 0)
            break;
          if (i > 200) {
            // time out!
            // System.err.println("Whoopsy! :P");
            break;
          }
        }
        if (Character.isDigit(word.charAt(0))) {
          setToolTipText(null);
          return super.getToolTipText(event);
        }
        String tooltipText = errorCheckerService.getASTGenerator()
            .getLabelForASTNode(line, word, xLS);

        //      log(errorCheckerService.mainClassOffset + " MCO "
        //      + "|" + line + "| offset " + xLS + word + " <= offf: "+off+ "\n");
        if (tooltipText != null) {
          return tooltipText;
        }
      }
    }
    // Used when there are Java tabs, but also the fall-through case from above
//    setToolTipText(null);
    return super.getToolTipText(event);
  }


  // TweakMode code
	protected int horizontalAdjustment = 0;

	public boolean interactiveMode = false;
//	public ArrayList<Handle> handles[];
//	public ArrayList<ColorControlBox> colorBoxes[];
	public List<List<Handle>> handles;
	public List<List<ColorControlBox>> colorBoxes;

	public Handle mouseHandle = null;
	public ColorSelector colorSelector;

	int cursorType;
	BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

	// Create a new blank cursor.
	Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
	    cursorImg, new Point(0, 0), "blank cursor");


	@Override
	synchronized public void paint(Graphics gfx) {
		super.paint(gfx);

		if (interactiveMode && handles != null) {
			int currentTab = getCurrentCodeIndex();
			// enable anti-aliasing
			Graphics2D g2d = (Graphics2D)gfx;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                				RenderingHints.VALUE_ANTIALIAS_ON);

			for (Handle n : handles.get(currentTab)) {
				// update n position and width, and draw it
				int lineStartChar = textArea.getLineStartOffset(n.line);
				int x = textArea.offsetToX(n.line, n.newStartChar - lineStartChar);
				int y = textArea.lineToY(n.line) + fm.getHeight() + 1;
				int end = textArea.offsetToX(n.line, n.newEndChar - lineStartChar);
				n.setPos(x, y);
				n.setWidth(end - x);
				n.draw(g2d, n==mouseHandle);
			}

			// draw color boxes
			for (ColorControlBox cBox: colorBoxes.get(currentTab)) {
				int lineStartChar = textArea.getLineStartOffset(cBox.getLine());
				int x = textArea.offsetToX(cBox.getLine(), cBox.getCharIndex() - lineStartChar);
				int y = textArea.lineToY(cBox.getLine()) + fm.getDescent();
				cBox.setPos(x, y+1);
				cBox.draw(g2d);
			}
		}
	}


	public void startInterativeMode() {
	  addMouseListener(this);
	  addMouseMotionListener(this);
	  interactiveMode = true;
	  setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		repaint();
	}


	public void stopInteractiveMode() {
		interactiveMode = false;

		if (colorSelector != null) {
			// close color selector
			colorSelector.hide();
			colorSelector.frame.dispatchEvent(new WindowEvent(colorSelector.frame, WindowEvent.WINDOW_CLOSING));
		}

		setCursor(new Cursor(Cursor.TEXT_CURSOR));
		repaint();
	}


	// Update the interface
	//public void updateInterface(ArrayList<Handle> handles[], ArrayList<ColorControlBox> colorBoxes[]) {
	public void updateInterface(List<List<Handle>> handles, List<List<ColorControlBox>> colorBoxes) {
		this.handles = handles;
		this.colorBoxes = colorBoxes;

		initInterfacePositions();
		repaint();
	}


	/**
	* Initialize all the number changing interfaces.
	* synchronize this method to prevent the execution of 'paint' in the middle.
	* (don't paint while we make changes to the text of the editor)
	*/
	public synchronized void initInterfacePositions() {
		SketchCode[] code = getEditor().getSketch().getCode();
		int prevScroll = textArea.getVerticalScrollPosition();
		String prevText = textArea.getText();

		for (int tab=0; tab<code.length; tab++) {
			String tabCode = getEditor().baseCode[tab];
			textArea.setText(tabCode);
			for (Handle n : handles.get(tab)) {
				int lineStartChar = textArea.getLineStartOffset(n.line);
				int x = textArea.offsetToX(n.line, n.newStartChar - lineStartChar);
				int end = textArea.offsetToX(n.line, n.newEndChar - lineStartChar);
				int y = textArea.lineToY(n.line) + fm.getHeight() + 1;
				n.initInterface(x, y, end-x, fm.getHeight());
			}

			for (ColorControlBox cBox : colorBoxes.get(tab)) {
				int lineStartChar = textArea.getLineStartOffset(cBox.getLine());
				int x = textArea.offsetToX(cBox.getLine(), cBox.getCharIndex() - lineStartChar);
				int y = textArea.lineToY(cBox.getLine()) + fm.getDescent();
				cBox.initInterface(this, x, y+1, fm.getHeight()-2, fm.getHeight()-2);
			}
		}

		textArea.setText(prevText);
		textArea.scrollTo(prevScroll, 0);
	}


	/**
	 * Take the saved code of the current tab and replace
	 * all numbers with their current values.
	 * Update TextArea with the new code.
	 */
	public void updateCodeText() {
		int charInc = 0;
		int currentTab = getCurrentCodeIndex();
		SketchCode sc = getEditor().getSketch().getCode(currentTab);
		String code = getEditor().baseCode[currentTab];

		for (Handle n : handles.get(currentTab)) {
			int s = n.startChar + charInc;
			int e = n.endChar + charInc;
			code = replaceString(code, s, e, n.strNewValue);
			n.newStartChar = n.startChar + charInc;
			charInc += n.strNewValue.length() - n.strValue.length();
			n.newEndChar = n.endChar + charInc;
		}

		replaceTextAreaCode(code);
		// update also the sketch code for later
		sc.setProgram(code);
	}


	// don't paint while we do the stuff below
	private synchronized void replaceTextAreaCode(String code) {
	  // by default setText will scroll all the way to the end
	  // remember current scroll position
	  int scrollLine = textArea.getVerticalScrollPosition();
	  int scrollHor = textArea.getHorizontalScrollPosition();
	  textArea.setText(code);
	  textArea.setOrigin(scrollLine, -scrollHor);
	}


	public String replaceString(String str, int start, int end, String put) {
		return str.substring(0, start) + put + str.substring(end, str.length());
	}


	public void updateCursor(int mouseX, int mouseY) {
		int currentTab = getCurrentCodeIndex();
		for (Handle n : handles.get(currentTab)) {
			if (n.pick(mouseX, mouseY)) {
				cursorType = Cursor.W_RESIZE_CURSOR;
				setCursor(new Cursor(cursorType));
				return;
			}
		}

		for (ColorControlBox colorBox : colorBoxes.get(currentTab)) {
			if (colorBox.pick(mouseX, mouseY)) {
				cursorType = Cursor.HAND_CURSOR;
				setCursor(new Cursor(cursorType));
				return;
			}
		}

		if (cursorType == Cursor.W_RESIZE_CURSOR ||
		    cursorType == Cursor.HAND_CURSOR ||
		    cursorType == -1) {
		  cursorType = Cursor.DEFAULT_CURSOR;
			setCursor(new Cursor(cursorType));
		}
	}


	private void showHideColorBoxes(int y) {
	  // display the box if the mouse if in the same line.
	  // always keep the color box of the color selector.
		int currentTab = getCurrentCodeIndex();

		boolean change = false;
		for (ColorControlBox box : colorBoxes.get(currentTab)) {
			if (box.setMouseY(y)) {
				change = true;
			}
		}

		if (colorSelector != null) {
			colorSelector.colorBox.visible = true;
		}

		if (change) {
			repaint();
		}
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseHandle != null) {
			// set the current drag amount of the arrows
			mouseHandle.setCurrentX(e.getX());

			// update code text with the new value
			updateCodeText();

			if (colorSelector != null) {
				colorSelector.refreshColor();
			}

			repaint();
		}
	}


	@Override
	public void mouseExited(MouseEvent e) {
	}


	@Override
	public void mousePressed(MouseEvent e) {
		int currentTab = getCurrentCodeIndex();
		// check for clicks on number handles
		for (Handle n : handles.get(currentTab)) {
			if (n.pick(e.getX(), e.getY())) {
				cursorType = -1;
				this.setCursor(blankCursor);
				mouseHandle = n;
				mouseHandle.setCenterX(e.getX());
				repaint();
				return;
			}
		}

		// check for clicks on color boxes
		for (ColorControlBox box : colorBoxes.get(currentTab)) {
			if (box.pick(e.getX(), e.getY())) {
				if (colorSelector != null) {
					// we already show a color selector, close it
					colorSelector.frame.dispatchEvent(new WindowEvent(colorSelector.frame, WindowEvent.WINDOW_CLOSING));
				}

				colorSelector = new ColorSelector(box);
				colorSelector.frame.addWindowListener(new WindowAdapter() {
				        public void windowClosing(WindowEvent e) {
				        	colorSelector.frame.setVisible(false);
				        	colorSelector = null;
				        }
				      });
				colorSelector.show(this.getLocationOnScreen().x + e.getX() + 30,
						this.getLocationOnScreen().y + e.getY() - 130);
			}
		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		if (mouseHandle != null) {
			mouseHandle.resetProgress();
			mouseHandle = null;

			updateCursor(e.getX(), e.getY());
			repaint();
		}
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		updateCursor(e.getX(), e.getY());

		if (!Settings.alwaysShowColorBoxes) {
			showHideColorBoxes(e.getY());
		}
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}


	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


	private JavaEditor getEditor() {
	  return ((JavaTextArea) textArea).editor;
	}


	private int getCurrentCodeIndex() {
    return getEditor().getSketch().getCurrentCodeIndex();
  }


//	private int getGutterMargins() {
//	  return ((JavaTextArea) textArea).getGutterMargins();
//	}


//	private int getGutterWidth() {
//	  return ((JavaTextArea) textArea).getGutterWidth();
//	}


	private JavaTextArea getTextArea() {
	  return (JavaTextArea) textArea;
	}


//	private Color getGutterBgColor() {
//    return ((JavaTextArea) textArea).gutterBgColor;
//  }


//	private boolean isDebugToolbarEnabled() {
//	  AtomicBoolean enabled = getEditor().debugToolbarEnabled;
//	  return (enabled != null && enabled.get());
//	}


//	private boolean hasJavaTabs() {
//	  return getEditor().hasJavaTabs();
//	}
}
