package org.boardgamers.wbc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * Created by Kevin on 5/30/2015. Fast scroll bar for filter list view. Sections set in Filter
 * Activity and received by Filter List Adapter, only containing letters of available tournaments
 */
public class FilterSideSelector extends View {

  public static final int BOTTOM_PADDING=10;

  private SectionIndexer selectionIndexer;
  private ListView listView;
  private Paint paint;
  private String[] sections;

  public FilterSideSelector(Context context) {
    super(context);
    init();
  }

  public FilterSideSelector(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public FilterSideSelector(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setBackgroundColor(0x44FFFFFF);
    paint=new Paint();
    paint.setColor(0xFFA6A9AA);
    paint.setTextSize(20);
    paint.setTextAlign(Paint.Align.CENTER);
  }

  public void setListView(ListView l) {
    listView=l;
    selectionIndexer=(SectionIndexer) l.getAdapter();

    Object[] sectionsArr=selectionIndexer.getSections();
    sections=new String[sectionsArr.length];
    for (int i=0; i<sectionsArr.length; i++) {
      sections[i]=sectionsArr[i].toString();
    }
  }

  public boolean onTouchEvent(@NonNull MotionEvent event) {
    super.onTouchEvent(event);
    int y=(int) event.getY();
    float selectedIndex=((float) y/(float) getPaddedHeight())*sections.length;

    if (event.getAction()==MotionEvent.ACTION_DOWN || event.getAction()==MotionEvent.ACTION_MOVE) {
      if (selectionIndexer==null) {
        selectionIndexer=(SectionIndexer) listView.getAdapter();
      }
      int position=selectionIndexer.getPositionForSection((int) selectedIndex);
      if (position==-1) {
        return true;
      }
      listView.setSelection(position);
    }
    return true;
  }

  protected void onDraw(Canvas canvas) {
    float charHeight=((float) getPaddedHeight())/(float) sections.length;
    float widthCenter=getMeasuredWidth()/2;
    for (int i=0; i<sections.length; i++) {
      canvas.drawText(sections[i], widthCenter, charHeight+(i*charHeight), paint);
    }
    super.onDraw(canvas);
  }

  private int getPaddedHeight() {
    return getHeight()-BOTTOM_PADDING;
  }
}