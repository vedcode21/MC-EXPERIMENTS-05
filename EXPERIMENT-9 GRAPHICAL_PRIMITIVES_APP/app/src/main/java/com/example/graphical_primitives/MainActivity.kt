package com.example.graphical_primitives

import android.app.Activity
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toolbar

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply a modern built-in theme programmatically
        setTheme(android.R.style.Theme_Material_Light_NoActionBar)
        super.onCreate(savedInstanceState)

        // Create the root layout with a black background
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        // Create a native Toolbar
        val toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.DKGRAY)
            title = "GRAPHICAL_PRIMITIVES_APP"
            setTitleTextColor(Color.WHITE)
        }
        // Layout parameters for the Toolbar
        val toolbarParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            150
        )
        rootLayout.addView(toolbar, toolbarParams)

        // Create an ImageView for drawing shapes
        val imageView = ImageView(this)
        // Layout parameters for ImageView — placed below the Toolbar
        val imageViewParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            topMargin = 150  // Leave space for the Toolbar
        }
        rootLayout.addView(imageView, imageViewParams)

        // Set the root layout as the content view
        setContentView(rootLayout)

        // Create a Bitmap and Canvas for drawing
        val bitmapWidth = 1400
        val bitmapHeight = 2000
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)
        imageView.setImageDrawable(BitmapDrawable(resources, bitmap))

        // Define constants for padding and text area
        val shapePadding = 80f     // Padding around shapes inside each box
        val textMargin = 100f      // Reserve bottom area in each box for text

        // Draw 6 grid boxes (2 columns x 3 rows)
        val gridPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        val boxes = listOf(
            RectF(  50f,   50f,  650f,  650f),  // Top-left
            RectF( 750f,   50f, 1350f,  650f),  // Top-right
            RectF(  50f,  700f,  650f, 1300f),  // Middle-left
            RectF( 750f,  700f, 1350f, 1300f),  // Middle-right
            RectF(  50f, 1350f,  650f, 1950f),  // Bottom-left
            RectF( 750f, 1350f, 1350f, 1950f)   // Bottom-right
        )
        boxes.forEach { box ->
            canvas.drawRect(box, gridPaint)
        }

        // Helper function: calculate the center of a box
        fun centerOf(box: RectF): PointF =
            PointF((box.left + box.right) / 2, (box.top + box.bottom) / 2)

        // Paint for drawing shapes (colors and fills)
        val shapePaint = Paint().apply {
            textSize = 60f
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Separate Paint for text labels (always white)
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // ---------- Draw shapes with labels in each box ----------

        // Box 1: Triangle (RED)
        val box1 = boxes[0]
        shapePaint.color = Color.RED
        val trianglePath = Path().apply {
            moveTo(centerOf(box1).x, box1.top + shapePadding)       // top vertex
            lineTo(box1.left + shapePadding, box1.bottom - textMargin) // bottom-left vertex
            lineTo(box1.right - shapePadding, box1.bottom - textMargin) // bottom-right vertex
            close()
        }
        canvas.drawPath(trianglePath, shapePaint)
        canvas.drawText("Triangle", box1.left + shapePadding, box1.bottom - 30f, textPaint)

        // Box 2: Ellipse (GREEN) with forced non-circular proportions
        val box2 = boxes[1]
        shapePaint.color = Color.GREEN
        val availableWidth2 = box2.width() - 2 * shapePadding
        // Force the ellipse's height to be 60% of its width to avoid a circle appearance
        val ellipseWidth = availableWidth2
        val ellipseHeight = ellipseWidth * 0.6f
        // Ensure the ellipse fits within the available height (considering text area)
        val finalEllipseHeight = if (ellipseHeight > (box2.height() - textMargin - 2 * shapePadding))
            (box2.height() - textMargin - 2 * shapePadding) else ellipseHeight
        // Center the ellipse horizontally; position it toward the top of box2
        val ellipseLeft = box2.left + shapePadding
        val ellipseTop = box2.top + shapePadding + ((box2.height() - textMargin - 2 * shapePadding - finalEllipseHeight) / 2)
        val ovalRect = RectF(
            ellipseLeft,
            ellipseTop,
            ellipseLeft + ellipseWidth,
            ellipseTop + finalEllipseHeight
        )
        canvas.drawOval(ovalRect, shapePaint)
        canvas.drawText("Ellipse", box2.left + shapePadding, box2.bottom - 30f, textPaint)

        // Box 3: Rectangle (BLUE) with non-square proportions
        val box3 = boxes[2]
        shapePaint.color = Color.BLUE
        val availableWidth3 = box3.width() - 2 * shapePadding
        val availableHeight3 = box3.height() - shapePadding - textMargin
        // Use 80% of available width and 50% of available height for the rectangle
        val rectWidth = availableWidth3 * 0.8f
        val rectHeight = availableHeight3 * 0.5f
        // Center the rectangle in the available area
        val rectLeft = box3.left + shapePadding + (availableWidth3 - rectWidth) / 2
        val rectTop = box3.top + shapePadding + (availableHeight3 - rectHeight) / 2
        val rectRect = RectF(rectLeft, rectTop, rectLeft + rectWidth, rectTop + rectHeight)
        canvas.drawRect(rectRect, shapePaint)
        canvas.drawText("Rectangle", box3.left + shapePadding, box3.bottom - 30f, textPaint)

        // Box 4: Circle (YELLOW)
        val box4 = boxes[3]
        shapePaint.color = Color.YELLOW
        val availableWidth4 = box4.width()
        val availableHeight4 = box4.height() - textMargin
        val diameter = minOf(availableWidth4, availableHeight4) - 2 * shapePadding
        val radius = diameter / 2
        val centerX4 = box4.left + box4.width() / 2
        // Position the circle in the top area of the box
        val centerY4 = box4.top + shapePadding + radius
        canvas.drawCircle(centerX4, centerY4, radius, shapePaint)
        canvas.drawText("Circle", box4.left + shapePadding, box4.bottom - 30f, textPaint)

        // Box 5: Square (MAGENTA)
        val box5 = boxes[4]
        shapePaint.color = Color.MAGENTA
        val availableSize5 = minOf(box5.width(), box5.height() - textMargin)
        val squareSide = availableSize5 - 2 * shapePadding
        val squareLeft = box5.left + (box5.width() - squareSide) / 2
        val squareTop = box5.top + shapePadding
        val squareRect = RectF(squareLeft, squareTop, squareLeft + squareSide, squareTop + squareSide)
        canvas.drawRect(squareRect, shapePaint)
        canvas.drawText("Square", box5.left + shapePadding, box5.bottom - 30f, textPaint)

        // Box 6: Line (CYAN) with thicker stroke
        val box6 = boxes[5]
        shapePaint.color = Color.CYAN
        shapePaint.strokeWidth = 15f  // Increase stroke width for the line
        val lineY = box6.top + (box6.height() - textMargin) / 2
        canvas.drawLine(box6.left + shapePadding, lineY, box6.right - shapePadding, lineY, shapePaint)
        canvas.drawText("Line", box6.left + shapePadding, box6.bottom - 30f, textPaint)
    }
}
