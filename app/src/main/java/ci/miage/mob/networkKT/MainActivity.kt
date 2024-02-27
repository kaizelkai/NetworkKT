package ci.miage.mob.networkKT

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ci.miage.mob.networkKT.Model.Arc
import ci.miage.mob.networkKT.Model.Graph
import ci.miage.mob.networkKT.Model.Noeud

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Créer un arc et un nœud
        val arc = Arc(1, Pair(0, 0), Pair(100, 100), "Arc 1", "Red", 2)
        val noeud = Noeud(1, Pair(50, 50), "Noeud 1", "Blue")

        // Ajouter l'arc et le nœud aux gestionnaires respectifs
        Graph.ArcManager.addArc(arc)
        Graph.NoeudManager.addNoeud(noeud)
    }
}
