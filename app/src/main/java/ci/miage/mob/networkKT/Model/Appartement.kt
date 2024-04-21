package ci.miage.mob.networkKT.Model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import ci.miage.mob.networkKT.R
import com.google.gson.Gson
import java.io.File

class Appartement(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var graph: Graphs?=null
    val noeuds: MutableList<Noeuds> = mutableListOf()
    val arcs: MutableList<Arcs> = mutableListOf()

    private val connectedArcs: MutableList<Arcs> = mutableListOf()

    private var addNodeMode: Boolean = false
    private var addConnectionMode: Boolean = false
    private var startPoint: PointF? = null
    private var endPoint: PointF? = null
    var modifyMode: Boolean = false
    private var modifyParam: Boolean = false

    private var touchedNode: Noeuds? = null
    private var touchedNodeCopy: Noeuds? = null

    private var touchOffsetX: Float = 0f
    private var touchOffsetY: Float = 0f
    private var moving = false

    private val gestureDetector: GestureDetector
    private val longPressHandler = Handler(Looper.getMainLooper())
    private var isLongPressing = false
    private var longPressedNode: Noeuds? = null
    private var longPressedArc: Arcs? = null

    val colors = listOf(
        Pair("Noir", Color.parseColor("#000000")),
        Pair("Rouge", Color.parseColor("#FF0101")),
        Pair("Vert", Color.parseColor("#04D80F")),
        Pair("Bleu", Color.parseColor("#0000ff")),
        Pair("Orange", Color.parseColor("#FF8324")),
        Pair("Cyan", Color.parseColor("#00FFFF")),
        Pair("Magenta", Color.parseColor("#FF00FF"))
    )

    val dialogView = LayoutInflater.from(context).inflate(R.layout.context_menu_layout, null)
    val buttonDeleteNode = dialogView.findViewById<Button>(R.id.deleteNode)
    val buttonModifyNode = dialogView.findViewById<Button>(R.id.modifyNode)
    val buttonDeleteArcs = dialogView.findViewById<Button>(R.id.deleteArc)
    val buttonModifyArc = dialogView.findViewById<Button>(R.id.modifyArc)

    val dialog = AlertDialog.Builder(context)
        .setView(dialogView)
        .setCancelable(true)
        .create()

    fun setGraphdt(graph: Graphs?) {
        this.graph = graph
        invalidate() // Demander à la vue de se redessiner avec le nouveau graphique
    }

    fun displayGraphs(graphs: List<Graphs>) {
        println("Liste des graphes enregistrés :")
        for ((index, graph) in graphs.withIndex()) {
            println("Graphique ${index + 1}:")
            println("Nom : ${graph.nomGraph}")
            println("Nombre de nœuds : ${graph.noeuds.size}")
            println("Nombre d'arcs : ${graph.arcs.size}")
            println()
        }
    }

    fun loadGraphFromJson(filePath: String) {
        val gson = Gson()
        val jsonString = File(filePath).readText()
        graph = gson.fromJson(jsonString, Graphs::class.java)
        invalidate() // Redessine la vue après le chargement des données
    }
    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                moving= false
                val node = findNearestNode(PointF(e.x, e.y))
                node?.let {
                    longPressedNode = node
                    val nodeIndex = noeuds.indexOf(node)
                    showCustomDialog(nodeIndex, node)
                }

                val arc = findNearestArc(PointF(e.x, e.y))
                arc?.let { selectedArc ->
                    longPressedArc = selectedArc
                    showCustomDialogForArc(selectedArc)
                }
            }
        })
        gestureDetector.setIsLongpressEnabled(true) // Activer la prise en charge des clics longs
    }

    private fun showModifyDialogNoeud(noeudselect: Noeuds) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.context_node_modify, null)
        val editTextFileName = dialogView.findViewById<EditText>(R.id.editTextName)
        val epaisseurName = dialogView.findViewById<EditText>(R.id.epaisseurName)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        // Remplir le spinner des couleurs avec les couleurs disponibles
        val colorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, colors.map { it.first })
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        // Préremplir les champs avec les valeurs actuelles du nœud
        editTextFileName.setText(noeudselect.etiquette)
        epaisseurName.setText(noeudselect.epaisseur.toString())
        val selectedColorIndex = colors.indexOfFirst { it.second == noeudselect.couleur }
        colorSpinner.setSelection(if (selectedColorIndex != -1) selectedColorIndex else 0)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonSave.setOnClickListener {
            val fileName = editTextFileName.text.toString()
            val fileNameepp = epaisseurName.text.toString()
            val selectedColor = colors[colorSpinner.selectedItemPosition]

            // Mettre à jour les attributs du nœud avec les nouvelles informations
            noeudselect.etiquette = fileName
            noeudselect.epaisseur = fileNameepp.toInt()
            noeudselect.couleur = selectedColor.second
            modifyParam = true

            // Rafraîchir la vue pour refléter les modifications apportées au nœud
            invalidate()

            Toast.makeText(context, "Nœud modifié : $fileName, Couleur : ${selectedColor.first}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showModifyDialogArc(arcSelect: Arcs) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.context_arc_modify, null)
        val arcName = dialogView.findViewById<EditText>(R.id.arcName)
        val arcEpaisseur = dialogView.findViewById<EditText>(R.id.arcEpaisseur)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.arcColorSpinner)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSaveArc)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancelArc)

        // Remplir le spinner des couleurs avec les couleurs disponibles
        val colorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, colors.map { it.first })
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        // Préremplir les champs avec les valeurs actuelles du nœud
        arcName.setText(arcSelect.etiquette)
        arcEpaisseur.setText(arcSelect.epaisseur.toString())
        val selectedColorIndex = colors.indexOfFirst { it.second == arcSelect.couleur }
        colorSpinner.setSelection(if (selectedColorIndex != -1) selectedColorIndex else 0)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonSave.setOnClickListener {
            val fileName = arcName.text.toString()
            val epaisseur = arcEpaisseur.text.toString()
            val selectedColor = colors[colorSpinner.selectedItemPosition]

            // Mettre à jour les attributs du nœud avec les nouvelles informations
            arcSelect.etiquette = fileName
            arcSelect.epaisseur = epaisseur.toInt()
            arcSelect.couleur = selectedColor.second
            modifyParam = true

            // Rafraîchir la vue pour refléter les modifications apportées au nœud
            invalidate()

            Toast.makeText(context, "Nœud modifié : $fileName, Couleur : ${selectedColor.first}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun showCustomDialog(index: Int, nodeset: Noeuds) {
        buttonDeleteNode.setOnClickListener {
            longPressedArc = null
            removeNoeud(nodeset)
            dialog.dismiss()
        }

        buttonModifyNode.setOnClickListener {
            longPressedArc = null
            showModifyDialogNoeud(nodeset)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showCustomDialogForArc(arcset: Arcs) {
        buttonDeleteArcs.setOnClickListener {
            longPressedNode = null
            removeArc(arcset)
            dialog.dismiss()

        }

        buttonModifyArc.setOnClickListener {
            longPressedNode = null
            showModifyDialogArc(arcset)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun calculateDistanceToArc(point: PointF, arc: Arcs): Float {
        // Coordonnées du point donné
        val x = point.x
        val y = point.y

        // Coordonnées des points de départ et de fin de l'arc
        val startX = arc.pointsDepart.first.toFloat()
        val startY = arc.pointsDepart.second.toFloat()
        val endX = arc.pointsFin.first.toFloat()
        val endY = arc.pointsFin.second.toFloat()

        // Calcul des coordonnées du point de l'arc le plus proche du point donné
        val closestX: Float
        val closestY: Float

        val dx = endX - startX
        val dy = endY - startY

        val t = ((x - startX) * dx + (y - startY) * dy) / (dx * dx + dy * dy)
        if (t < 0) {
            closestX = startX
            closestY = startY
        } else if (t > 1) {
            closestX = endX
            closestY = endY
        } else {
            closestX = startX + t * dx
            closestY = startY + t * dy
        }

        // Calcul de la distance entre le point donné et le point de l'arc le plus proche
        val distanceX = x - closestX
        val distanceY = y - closestY
        return Math.sqrt((distanceX * distanceX + distanceY * distanceY).toDouble()).toFloat()
    }

    private fun findNearestArc(point: PointF): Arcs? {
        var nearestArc: Arcs? = null
        var shortestDistance = Float.MAX_VALUE
        for (arc in arcs) {
            val distance = calculateDistanceToArc(point, arc)
            if (distance < shortestDistance) {
                nearestArc = arc
                shortestDistance = distance
            }
        }
        return nearestArc
    }

    fun toggleAddNodeMode() {
        addNodeMode = !addNodeMode
        addConnectionMode = false
        modifyMode = false
    }

    fun toggleAddConnectionMode() {
        addConnectionMode = true
        modifyMode = false
    }

    fun clearContent() {
        // Effacer la liste de noeuds et d'arcs
        noeuds.clear()
        arcs.clear()
        invalidate()
    }

    fun setModifyMode() {
        modifyMode = true
        addConnectionMode = false

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Vérifie si le graphique est chargé
        if (graph != null) {
            // Dessiner tous les arcs
            for (arc in graph!!.arcs) {
                val paint = Paint().apply {
                    color = arc.couleur
                    style = Paint.Style.STROKE
                    strokeWidth = arc.epaisseur.toFloat()
                }

                canvas.drawLine(
                    arc.pointsDepart.first.toFloat(),
                    arc.pointsDepart.second.toFloat(),
                    arc.pointsFin.first.toFloat(),
                    arc.pointsFin.second.toFloat(),
                    paint
                )
            }

            // Dessiner les nœuds
            for (noeud in graph!!.noeuds) {
                canvas.drawCircle(
                    noeud.point.first.toFloat(),
                    noeud.point.second.toFloat(),
                    20f,
                    Paint().apply {
                        color = noeud.couleur
                        style = Paint.Style.FILL
                        strokeWidth = noeud.epaisseur.toFloat()
                    }
                )
            }
        }


        // Dessiner tous les arcs
        for (arc in arcs) {
            val paint = Paint().apply {
                color = arc.couleur
                style = Paint.Style.STROKE
                strokeWidth = arc.epaisseur.toFloat()
            }

            canvas.drawLine(
                arc.pointsDepart.first.toFloat(),
                arc.pointsDepart.second.toFloat(),
                arc.pointsFin.first.toFloat(),
                arc.pointsFin.second.toFloat(),
                paint
            )
        }

        // Dessiner les noeuds
        for (noeud in noeuds) {
            if (modifyParam &&  noeud.epaisseur !=20) {
                val labelX = if (noeud == touchedNode) {
                    (noeud.point.first + touchOffsetX).toFloat()
                } else {
                    noeud.point.first.toFloat()
                }
                val labelY = if (noeud == touchedNode) {
                    (noeud.point.second + touchOffsetY).toFloat()
                } else {
                    noeud.point.second.toFloat()
                }
                // Obtenir les dimensions réelles du texte
                val textPaint = Paint().apply {
                    color = Color.BLUE
                }
                val textBounds = Rect()
                textPaint.getTextBounds(noeud.etiquette, 0, noeud.etiquette.length, textBounds)
                // Dessiner le rectangle en arrière-plan du texte
                val rectLeft = labelX
                val rectTop = labelY + textBounds.top
                val rectRight = labelX + textBounds.width()
                val rectBottom = labelY + textBounds.bottom
                canvas.drawRect(
                    rectLeft,
                    rectTop,
                    rectRight,
                    rectBottom,
                    Paint().apply {
                        color = Color.BLUE
                    }
                )
                // Dessiner le texte par-dessus le rectangle
                canvas.drawText(noeud.etiquette, labelX, labelY, Paint().apply {
                    color = noeud.couleur
                    textSize = noeud.epaisseur.toFloat()
                })
            } else {
                canvas.drawCircle(
                    noeud.point.first.toFloat(),
                    noeud.point.second.toFloat(),
                    20f,
                    Paint().apply {
                        color = noeud.couleur
                        style = Paint.Style.FILL
                        strokeWidth = noeud.epaisseur.toFloat()
                    }
                )
            }
        }

        // Dessiner la ligne en cours de traçage si addConnectionMode est true
        if (addConnectionMode && startPoint != null && endPoint != null) {
            val paint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawLine(startPoint!!.x, startPoint!!.y, endPoint!!.x, endPoint!!.y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (addConnectionMode) {
                    // Début du traçage de ligne seulement si addConnectionMode est true
                    startPoint = PointF(event.x, event.y)
                    endPoint = PointF(event.x, event.y)
                    invalidate()
                }
                if (modifyMode) {
                    touchedNode = findNearestNode(PointF(event.x, event.y))
                    touchedNodeCopy = touchedNode
                    if (touchedNode != null) {
                        touchOffsetX = event.x - touchedNode!!.point.first.toFloat()
                        touchOffsetY = event.y - touchedNode!!.point.second.toFloat()
                        moving = true

                        connectedArcs.clear()
                        for (arc in arcs) {
                            if (arc.pointsDepart == touchedNode!!.point || arc.pointsFin == touchedNode!!.point) {
                                connectedArcs.add(arc)
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (addConnectionMode) {
                    // Mise à jour de la position du point d'arrivée seulement si addConnectionMode est true
                    endPoint = PointF(event.x, event.y)
                    invalidate()
                }
                if (modifyMode && moving && touchedNode != null) {
                    touchedNode!!.point = Pair(event.x.toDouble() - touchOffsetX.toDouble(), event.y.toDouble() - touchOffsetY.toDouble())
                    // Mettre à jour les coordonnées des arcs connectés au nœud déplacé
                    for (arc in connectedArcs) {
                        if (arc.pointsDepart == touchedNode!!.point) {
                            arc.pointsDepart = touchedNode!!.point
                        }
                        if (arc.pointsFin == touchedNode!!.point) {
                            arc.pointsFin = touchedNode!!.point
                        }
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (addConnectionMode && startPoint != null && endPoint != null) {
                    val nearestNode = findNearestNode(endPoint!!)
                    nearestNode?.let { node ->
                        // Créer un nouvel arc si un nœud est trouvé
                        val startNode = findNearestNode(startPoint!!)
                        startNode?.let { start ->
                            val newArc = Arcs(
                                idArc = arcs.size + 1,
                                pointsDepart = start.point,
                                pointsFin = node.point,
                                etiquette = "Arc ${arcs.size + 1}",
                                couleur = Color.parseColor("#00000A"),
                                epaisseur = 4
                            )
                            arcs.add(newArc)
                            invalidate()
                        }
                    }
                }
                moving = false
                startPoint = null
                endPoint = null

                longPressHandler.removeCallbacksAndMessages(null)
                // Réinitialise l'état du maintien long
                isLongPressing = false
                longPressedNode = null
            }
        }
        return true
    }

    private fun findNearestNode(point: PointF): Noeuds? {
        var nearestNode: Noeuds? = null
        var shortestDistance = Float.MAX_VALUE
        for (node in noeuds) {
            val distance = calculateDistance(point, node.point)
            if (distance < shortestDistance) {
                nearestNode = node
                shortestDistance = distance
            }
        }
        return nearestNode
    }

    private fun calculateDistance(point1: PointF, point2: Pair<Double, Double>): Float {
        val dx = point1.x - point2.first.toFloat()
        val dy = point1.y - point2.second.toFloat()
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    private fun removeNoeud(noeud: Noeuds) {
        noeuds.remove(noeud)
        val connectedArcsToRemove = arcs.filter { it.pointsDepart == noeud.point || it.pointsFin == noeud.point }
        arcs.removeAll(connectedArcsToRemove)
        connectedArcs.removeAll(connectedArcsToRemove)
        invalidate()
    }

    private fun removeArc(arc: Arcs) {
        arcs.remove(arc)
        invalidate()
    }

    fun addArc(arc: Arcs){
        arcs.add(arc)
        invalidate()
    }

    fun addNoeud(noeud: Noeuds) {
        noeuds.add(noeud)
        NoeudManager.addNoeud(noeud)
        invalidate() // Redessine le graphique
    }

    object NoeudManager {
        private val noeuds: MutableMap<Int, Noeuds> = mutableMapOf()

        fun addNoeud(noeud: Noeuds) {
            noeuds[noeud.idNoeud] = noeud
        }
    }
}
