package com.heitem.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Graphique  extends View {

	public Graphique(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	public Graphique(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public Graphique(Context context, AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
	}
	
	 
	private Paint paint ;
	private Canvas canvas ;
	 

	protected void onDraw(final Canvas canvas) {
		paint = new Paint();
		this.canvas = canvas;
		canvas.drawColor(Color.TRANSPARENT);
	 
		 
	 	paint.setColor(Color.RED);
		canvas.drawCircle(50, 50, 40, paint);
		paint.setColor(Color.BLACK);
		 
        canvas.drawLine(0, 0, 100, 100, paint);
	}
	 
	 
	 

}
