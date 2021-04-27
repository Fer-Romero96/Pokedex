package edu.itesm.pokedex

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.itesm.pokedex.databinding.ActivityPokemonsBinding

abstract class SwipeToDelete (context: Context,
                              direccion: Int, direccionArrastre: Int):
        ItemTouchHelper.SimpleCallback(direccion, direccionArrastre){
    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

}



class PokemonsActivity : AppCompatActivity() {

    private lateinit var bind : ActivityPokemonsBinding //Elementos graficos de la interfaz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPokemonsBinding.inflate(layoutInflater)
        setContentView(bind.root)

        cargaDatos()
    }

    private fun borraPokemon(pokemon : Pokemon){
        val storage = FirebaseStorage.getInstance().getReferenceFromUrl(pokemon.foto)
        storage.delete().addOnSuccessListener {
            val usuario = Firebase.auth.currentUser
            val referencia = FirebaseDatabase.getInstance().getReference("pokemons/${usuario.uid}/${pokemon.id}")

            referencia.removeValue()

        }
    }

    private fun cargaDatos(){
        var reference : DatabaseReference
        var database : FirebaseDatabase

        database = FirebaseDatabase.getInstance()
        val usuario = Firebase.auth.currentUser
        reference = database.getReference("pokemons/${usuario.uid}")

        bind.recyclerPokemon.apply {

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var listaPokemons = ArrayList<Pokemon>()
                    for (pokemon in snapshot.children) {
                        var objeto = pokemon.getValue(Pokemon::class.java)
                        listaPokemons.add(objeto as Pokemon)
                    }

                    if(listaPokemons.isEmpty()){
                        Toast.makeText(this@PokemonsActivity, "Error al obtener datos", Toast.LENGTH_LONG).show()
                    }
                    adapter = PokemonAdapter(listaPokemons)
                    layoutManager = LinearLayoutManager(this@PokemonsActivity)

                    val item = object : SwipeToDelete(this@PokemonsActivity,
                            ItemTouchHelper.UP,ItemTouchHelper.LEFT){
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            super.onSwiped(viewHolder, direction)
                            val pokemon = listaPokemons[ viewHolder.adapterPosition ]

                            borraPokemon(pokemon)

                        }
                    }
                    val itemTouchHelper = ItemTouchHelper(item)
                    itemTouchHelper.attachToRecyclerView(bind.recyclerPokemon)

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PokemonsActivity, "Error al obtener datos", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}