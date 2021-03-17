package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.piotrjastrzebski.playground.PLog;

/**
 * Custom version with few extra getters
 */
public class PTextArea extends TextArea {
    public PTextArea (String text, Skin skin) {
        super(text, skin);
    }

    public PTextArea (String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public PTextArea (String text, TextFieldStyle style) {
        super(text, style);
    }

    public void setSelectionStart (float x, float y) {
        // x,y local coordinates
        // this is similar to setCursorPosition in TextAreaListener
        Drawable background = style.background;
        BitmapFont font = style.font;

        float height = getHeight();

        if (background != null) {
            height -= background.getTopHeight();
            x -= background.getLeftWidth();
        }
        x = Math.max(0, x);
        if (background != null) {
            y -= background.getTopHeight();
        }
        // we need to change this for letterUnderCursor(x) :<
        int tmp = cursorLine;

        int endCursorLine = (int)Math.floor((height - y) / font.getLineHeight()) + firstLineShowing;
        endCursorLine = Math.max(0, Math.min(endCursorLine, getLines() - 1));

        cursorLine = endCursorLine;
        int id = letterUnderCursor(x);
        cursorLine = tmp;

//        PLog.log("Start char '" + getText().charAt(id)+"'");

//        int cs = getSelectionStart();
//        int ce = getSelectionStart() + getSelection().length();
//
//        int start = Math.min(cs, id);
//        int end = Math.max(ce, id);
        setSelection(id, getSelectionStart() + getSelection().length());
    }

    public void setSelectionEnd (float x, float y) {
        // x,y local coordinates
        // this is similar to setCursorPosition in TextAreaListener

        Drawable background = style.background;
        BitmapFont font = style.font;

        float height = getHeight();

        if (background != null) {
            height -= background.getTopHeight();
            x -= background.getLeftWidth();
        }
        x = Math.max(0, x);
        if (background != null) {
            y -= background.getTopHeight();
        }
        // we need to change this for letterUnderCursor(x) :<
        int tmp = cursorLine;

        int endCursorLine = (int)Math.floor((height - y) / font.getLineHeight()) + firstLineShowing;
        endCursorLine = Math.max(0, Math.min(endCursorLine, getLines() - 1));

        cursorLine = endCursorLine;
        int id = letterUnderCursor(x);
        cursorLine = tmp;

//        PLog.log("End char '" + getText().charAt(id)+"'");

//        int cs = getSelectionStart();
//        int ce = getSelectionStart() + getSelection().length();
//
//        int start = Math.min(cs, id);
//        int end = Math.max(ce, id);
//        setSelection(start, end);

        setSelection(getSelectionStart(), id);
    }

    @Override
    public void setSelection (int selectionStart, int selectionEnd) {
        PLog.log("setSelection (" + selectionStart + ", " + selectionEnd + ")");
        super.setSelection(selectionStart, selectionEnd);
    }

    public Vector2 getSelectionStart (Vector2 out) {
        int i = firstLineShowing * 2;
        float offsetY = 0;
        int minIndex = Math.min(cursor, selectionStart);
        int maxIndex = Math.max(cursor, selectionStart);
        BitmapFont.BitmapFontData fontData = style.font.getData();
        while (i + 1 < linesBreak.size && i < (firstLineShowing + getLinesShowing()) * 2) {

            int lineStart = linesBreak.get(i);
            int lineEnd = linesBreak.get(i + 1);

            if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
                || (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

                int start = Math.max(lineStart, minIndex);

                float fontLineOffsetX = 0;
                // we can't use fontOffset as it is valid only for first glyph/line in the text
                // we will grab first character in this line and calculate proper offset for this line
                BitmapFont.Glyph lineFirst = fontData.getGlyph(displayText.charAt(lineStart));
                if (lineFirst != null && start != lineStart) {
                    // see BitmapFontData.getGlyphs()#852 for offset calculation
                    // if selection starts when line starts we want to offset width instead of moving the start as it looks better
                    fontLineOffsetX = lineFirst.fixedWidth? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
                }
                float selectionX = glyphPositions.get(start) - glyphPositions.get(lineStart);

                Drawable drawable = getBackgroundDrawable();
                if (drawable != null) {
                    out.x = drawable.getLeftWidth() + selectionX + fontLineOffsetX;
                } else {
                    out.x = selectionX + fontLineOffsetX;
                }
                out.y = getTextY(style.font, drawable) - /*lineHeight -*/ offsetY;
                break;
            }

            offsetY += style.font.getLineHeight();
            i += 2;
        }
        return out;
    }

    public Vector2 getSelectionEnd (Vector2 out) {
        int i = Math.min((firstLineShowing + getLinesShowing()) * 2, linesBreak.size -2);
        int minIndex = Math.min(cursor, selectionStart);
        int maxIndex = Math.max(cursor, selectionStart);
        BitmapFont.BitmapFontData fontData = style.font.getData();
        while (i >= firstLineShowing * 2) {
            int lineStart = linesBreak.get(i);
            int lineEnd = linesBreak.get(i + 1);

            if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
                || (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

                int start = Math.max(lineStart, minIndex);
                int end = Math.min(lineEnd, maxIndex);

                float fontLineOffsetX = 0;
                // we can't use fontOffset as it is valid only for first glyph/line in the text
                // we will grab first character in this line and calculate proper offset for this line
                BitmapFont.Glyph lineFirst = fontData.getGlyph(displayText.charAt(lineStart));
                if (lineFirst != null && start != lineStart) {
                    // see BitmapFontData.getGlyphs()#852 for offset calculation
                    // if selection starts when line starts we want to offset width instead of moving the start as it looks better
                    fontLineOffsetX = lineFirst.fixedWidth? 0 : -lineFirst.xoffset * fontData.scaleX - fontData.padLeft;
                }
                float selectionX = glyphPositions.get(start) - glyphPositions.get(lineStart);
                float selectionWidth = glyphPositions.get(end) - glyphPositions.get(start);

                Drawable drawable = getBackgroundDrawable();
                if (drawable != null) {
                    out.x = drawable.getLeftWidth() + selectionX + fontLineOffsetX + selectionWidth;
                } else {
                    out.x = selectionX + fontLineOffsetX + selectionWidth;
                }
                out.y = getTextY(style.font, drawable) - (i *.5f + 1) /* * lineHeight */;
                break;
            }
            i -= 2;
        }
        return out;
    }
}
