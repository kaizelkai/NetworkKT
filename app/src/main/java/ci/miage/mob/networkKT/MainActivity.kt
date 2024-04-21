package ci.miage.mob.networkKT

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ci.miage.mob.networkKT.Model.Appartement
import ci.miage.mob.networkKT.Model.Graphs
import ci.miage.mob.networkKT.Model.Noeuds
import com.google.gson.Gson
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var appartement: Appartement // Déclarer une variable pour votre vue personnalisée

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        appartement = findViewById(R.id.appartement)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset_network -> {
                appartement.clearContent()
                showToast("Reseau reinitiliser")
                true
            }
            R.id.save_network -> {
                // Action for save network
                showToast("Demande de sauvegarde")
                showSaveDialog()
                true
            }
            R.id.display_saved_network -> {
                // Action for display saved network
                showToast("Liste des reseaux sauvegarder")
                showSavedNetworksMenu()
                true
            }
            R.id.modes -> {
                // Action pour modes
                true
            }
            R.id.add_objects_mode -> {
                // Action pour ajouter objects
                showToast("Objet ajouter")
                val randomX = (Math.random() * appartement.width).toFloat()
                val randomY = (Math.random() * appartement.height).toFloat()
                val newNode = Noeuds(
                    idNoeud = appartement.noeuds.size + 1,
                    point = Pair(randomX.toDouble(), randomY.toDouble()),
                    etiquette = "Node ${appartement.noeuds.size + 1}",
                    couleur = appartement.colors.first { it.first == "Noir" }.second,
                    epaisseur = 20
                )

                appartement.addNoeud(newNode)
                appartement.toggleAddNodeMode() // Activer le mode d'ajout de nœud
                true
            }
            R.id.add_connections_mode -> {
                // Action pour ajouter connections
                showToast("Connection possible")
                appartement.toggleAddConnectionMode()
                true
            }
            R.id.modify_objects_connections_mode -> {
                // Action modifier objects/connections
                showToast("Modifier objet ou connection")
                appartement.setModifyMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSaveDialog() {
        val dialogView = layoutInflater.inflate(R.layout.layout_save_dialog, null)
        val editTextFileName = dialogView.findViewById<EditText>(R.id.editTextFileName)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonSave.setOnClickListener {
            val fileName = editTextFileName.text.toString()
            // Enregistrer le fichier avec le nom `fileName`
            val graph = Graphs(1, fileName, appartement.noeuds, appartement.arcs)
            saveGraphToFile(graph, fileName)
            Toast.makeText(this, "Fichier sauvegardé sous le nom : $fileName", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveGraphToFile(graph: Graphs, fileName: String) {
        /*val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString: String = gson.toJson(graph)
        File(fileName).writeText(jsonString)
*/      if (graph != null) {
            val gson = Gson()
            val json = gson.toJson(graph)

            // Enregistrer le JSON dans un fichier
            val file = File(this.filesDir, "$fileName.json")
            file.writeText(json)
        }
        else {
            showToast("Aucun graphique trouvé dans le fichier.")
        }
    }

    private fun showSavedNetworksMenu() {
        val savedNetworkFiles = getListOfSavedFiles() // Récupérer la liste des fichiers JSON sauvegardés

        if (savedNetworkFiles.isEmpty()) {
            showToast("Aucun réseau sauvegardé trouvé")
            return
        }

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Réseaux sauvegardés")

        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedNetworkFiles)
        dialogBuilder.setAdapter(arrayAdapter) { _, position ->
            val selectedFileName = savedNetworkFiles[position]
            val selectedGraphs = loadFromJson(selectedFileName) // Charger les graphiques à partir du fichier sélectionné
            displayGraphs(selectedGraphs) // Afficher les graphiques
        }

        dialogBuilder.setNegativeButton("Fermer", null)

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun getListOfSavedFiles(): List<String> {
        val filesDir = this.filesDir
        return filesDir.list { _, name -> name.endsWith(".json") }.toList()
    }

    private fun loadFromJson(filePath: String): Graphs?{
        val gson = Gson()
        val jsonString = File(filePath).readText()
        return gson.fromJson(jsonString, Graphs::class.java)
    }

    private fun displayGraphs(graphs: Graphs?) {
        // Créer les noeuds du graphique
        for (noeud in graphs!!.noeuds) {
            // Ajouter chaque noeud à la vue personnalisée
            appartement.addNoeud(noeud)
        }
        // Créer les arcs du graphique
        for (arc in graphs!!.arcs) {
            // Ajouter chaque arc à la vue personnalisée
            appartement.addArc(arc)
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
