package edu.itesm.pokedex

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

data class Pokemon(val id: String, val nombre: String,
                   val tipo: String, val latitude: Double, val longitude: Double, val foto: String){
    constructor():this("","", "", 0.0,0.0,"")
}


class MainActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    // Incluye las variables de Analytics:
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var bundle: Bundle

    // puede tener el valor que quieras
    private val RICapture = 10007

    // Ultima coordenada del dispositivo
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // Cachar la foto
    private lateinit var foto:Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Write a message to the database
        // val database = FirebaseDatabase.getInstance()
        // val myRef = database.getReference("message")

        // myRef.setValue("Hello, World!")
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("pokemons")

        //inicializa las variables:
        analytics = FirebaseAnalytics.getInstance(this)
        bundle = Bundle()


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        activaReferencia()

    }

    private fun activaReferencia(){
        // checar los permisos
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 10007)
        }
    }

    public fun logout(view: View){
        Firebase.auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    public fun fotoPokemon(view: View){

        // Lanzar el intento para abrir la camara
        val tomaFoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // el RICapture es un identificador
        startActivityForResult(tomaFoto, RICapture)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RICapture && resultCode == RESULT_OK){
            foto = data?.extras?.get("data") as Bitmap

        }
    }

    public fun addPokemon(view: View){
        val nombre = findViewById<EditText>(R.id.nombre).text
        val tipo = findViewById<EditText>(R.id.tipo).text
        if(nombre.isNotEmpty() && nombre.isNotBlank() && tipo.isNotEmpty() && tipo.isNotBlank()){
            var latitude = 0.0
            var longitude = 0.0
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.latitude
                                longitude = location.longitude
                            }
                        }
            }
            if( foto != null){

                // Convertir a bytes la foto
                val baos = ByteArrayOutputStream()
                foto.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                // ID unico
                val fileName = UUID.randomUUID().toString()
                val storage_reference = FirebaseStorage.getInstance().getReference("/pokefotos/$fileName")
                val uploadTask = storage_reference.putBytes(data)

                // si se ejecuta bien la tarea de subir la imagen
                uploadTask.addOnSuccessListener {
                    // si se obtuvo el url de la imagen en storage
                    storage_reference.downloadUrl.addOnSuccessListener {
                        // grabo el pokemon en RealTime database
                        val id = reference.push().key
                        val pokemon = Pokemon(
                                id.toString(),
                                nombre.toString(),
                                tipo.toString(),
                                latitude,
                                longitude,
                                it.toString()
                        )
                        reference.child(id!!).setValue(pokemon)
                        nombre.clear()
                        tipo.clear()
                        Toast.makeText(this,"PoKEMON CAPTURADO!", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, "Error al subir un pokemon", Toast.LENGTH_LONG).show()
                }
            }
            bundle.putString("edu_itesm_pokedex_main", "added_pokemon")
            analytics.logEvent("main", bundle)
        }else{
            Toast.makeText(applicationContext, "error en nombre o tipo!", Toast.LENGTH_LONG).show()
        }
    }

    /*
    public fun addPokemon(view: View){
        val nombre = findViewById<EditText>(R.id.nombre).text
        val tipo = findViewById<EditText>(R.id.tipo).text

        if(nombre.isNotEmpty() && nombre.isNotBlank() && tipo.isNotEmpty() && tipo.isNotBlank()){

            var latitude = 0.0
            var longitude = 0.0

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.latitude
                                longitude = location.longitude
                            }

                        }

            }

            if(foto != null){

                //Convertir a bytes la foto
                val baos = ByteArrayOutputStream()
                foto.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()


                val fileName = UUID.randomUUID().toString()
                val storage_reference = FirebaseStorage.getInstance().getReference("/pokefotos/$fileName")
                val uploadTask = storage_reference.putBytes(data)

                // Tarea Asincrona
                uploadTask.addOnSuccessListener {
                    storage_reference.downloadUrl.addOnSuccessListener {
                        val id = reference.push().key
                        val pokemon = Pokemon(
                                id.toString(),
                                nombre.toString(),
                                tipo.toString(),
                                latitude,
                                longitude,
                                it.toString()
                        )
                        reference.child(id!!).setValue(pokemon)
                        nombre.clear()
                        tipo.clear()
                        Toast.makeText(this,"POKEMON CAPTURADO!", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, "Error al subir un pokemon", Toast.LENGTH_LONG).show()
                }

            }


            val pokemon = Pokemon(nombre.toString(), tipo.toString())
            val id = reference.push().key
            reference.child(id!!).setValue(pokemon) // !! Significa que puede enviar algo nulo o no nulo

            // Evento hacia analytics
            bundle.putString("edu_itesm_pokedex_main", "added_pokemon")
            analytics.logEvent("main",bundle)

        }else{

            Toast.makeText(applicationContext, "Error en nombre o Tipo!", Toast.LENGTH_LONG).show()

        }
    }*/

    public fun getPokemon(view : View){

        reference.addValueEventListener(object  : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                var listaPokemons = ArrayList<Pokemon>()

                for (pokemon in snapshot.children){
                    val objeto = pokemon.getValue(Pokemon::class.java)
                    listaPokemons.add(objeto as Pokemon)
                }

                Log.i("Pokemons", listaPokemons.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}