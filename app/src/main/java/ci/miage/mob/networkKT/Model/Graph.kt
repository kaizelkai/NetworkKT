package ci.miage.mob.networkKT.Model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import ci.miage.mob.networkKT.R

class Graph(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val idGragh: Int
    val listeArc: MutableList<Int>
    val listeNoeud: MutableList<Int>
    val nonGraph: String

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Graph)
        idGragh = typedArray.getInt(R.styleable.Graph_idGragh, 0)
        listeArc = typedArray.getTextArray(R.styleable.Graph_listeArc).map { it.toString().toInt() }.toMutableList()
        listeNoeud = typedArray.getTextArray(R.styleable.Graph_listeNoeud).map { it.toString().toInt() }.toMutableList()
        nonGraph = typedArray.getString(R.styleable.Graph_nonGraph) ?: ""
        typedArray.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        // Dessiner les arcs
        for (arcId in listeArc) {
            val arc = ArcManager.getArcById(arcId)
            canvas.drawLine(
                arc?.pointsDepart?.first?.toFloat() ?: 0f,
                arc?.pointsDepart?.second?.toFloat() ?: 0f,
                arc?.pointFin?.first?.toFloat() ?: 0f,
                arc?.pointFin?.second?.toFloat() ?: 0f,
                paint
            )
        }


        // Dessiner les noeuds
        for (noeudId in listeNoeud) {
            val noeud = NoeudManager.getNoeudById(noeudId)
            canvas.drawCircle(
                noeud?.points?.first?.toFloat() ?: 0f,
                noeud?.points?.second?.toFloat() ?: 0f,
                20f, paint
            )
        }

    }

    object ArcManager {
        private val arcs: MutableMap<Int, Arc> = mutableMapOf()

        fun addArc(arc: Arc) {
            arcs[arc.idArc] = arc
        }

        fun removeArc(arcId: Int) {
            arcs.remove(arcId)
        }

        fun updateArc(arcId: Int, newArc: Arc) {
            arcs[arcId] = newArc
        }

        fun getArcById(arcId: Int): Arc? {
            return arcs[arcId]
        }
    }

    object NoeudManager {
        private val noeuds: MutableMap<Int, Noeud> = mutableMapOf()

        fun addNoeud(noeud: Noeud) {
            noeuds[noeud.idNoeud] = noeud
        }

        fun removeNoeud(noeudId: Int) {
            noeuds.remove(noeudId)
        }

        fun updateNoeud(noeudId: Int, newNoeud: Noeud) {
            noeuds[noeudId] = newNoeud
        }

        fun getNoeudById(noeudId: Int): Noeud? {
            return noeuds[noeudId]
        }
    }
}
