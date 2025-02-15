package com.example.database_app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.reflect.TypeToken

// Define theme colors
val DarkBackground = Color.Black
val PrimaryBlue =Color(0xFF00416A)
val WhiteText = Color.White

// Data Model
data class Item(
    val id: Int,
    val name: String,
    var stock: Int,
    var price: Double
)

typealias ItemsList = List<Item>

class InventoryViewModel(private val context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _items = MutableStateFlow(loadItems())
    val items: StateFlow<ItemsList> = _items

    private fun loadItems(): ItemsList {
        val json = sharedPreferences.getString("inventory_list", "[]")
        val type = object : TypeToken<ItemsList>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveItems(items: ItemsList) {
        sharedPreferences.edit().putString("inventory_list", gson.toJson(items)).apply()
        _items.value = items
    }

    fun addItem(item: Item) {
        val updatedList = _items.value.toMutableList().apply { add(item) }
        saveItems(updatedList)
    }

    fun updateItem(updatedItem: Item) {
        val updatedList = _items.value.map { if (it.id == updatedItem.id) updatedItem else it }
        saveItems(updatedList)
    }

    fun deleteItem(item: Item) {
        val updatedList = _items.value.filter { it.id != item.id }
        saveItems(updatedList)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InventoryViewModel(applicationContext) as T
            }
        })[InventoryViewModel::class.java]

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") { InventoryApp(viewModel, navController) }
                composable("checkout") { CheckoutInventoryScreen(viewModel, navController) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp(viewModel: InventoryViewModel, navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory App", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WhiteText) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = PrimaryBlue)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
            ) {
                Text("Add Item", fontSize = 18.sp, color = WhiteText)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("checkout") },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
            ) {
                Text("Checkout Inventory", fontSize = 18.sp, color = WhiteText)
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(onDismiss = { showAddDialog = false }, viewModel)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutInventoryScreen(viewModel: InventoryViewModel, navController: NavController) {
    val items by viewModel.items.collectAsState()
    var editItem by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Checkout Inventory", color = WhiteText) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // Table Headers with Bigger Text
            Row(
                Modifier.fillMaxWidth().padding(8.dp).background(PrimaryBlue),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Item Name",
                    fontWeight = FontWeight.Bold,
                    color = WhiteText,
                    fontSize = 20.sp, // Increased text size
                    modifier = Modifier.weight(1f).padding(8.dp)
                )
                Text(
                    "Price",
                    fontWeight = FontWeight.Bold,
                    color = WhiteText,
                    fontSize = 20.sp, // Increased text size
                    modifier = Modifier.weight(0.5f).padding(8.dp)
                )
                Text(
                    "Stock",
                    fontWeight = FontWeight.Bold,
                    color = WhiteText,
                    fontSize = 20.sp, // Increased text size
                    modifier = Modifier.weight(0.5f).padding(8.dp)
                )
            }

            // Inventory Items List
            LazyColumn {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editItem = item }
                            .padding(8.dp)
                            .background(Color.DarkGray)
                    ) {
                        Text(item.name, color = WhiteText, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(8.dp))
                        Text("₹${item.price}", color = WhiteText, fontSize = 14.sp, modifier = Modifier.weight(0.5f).padding(8.dp))
                        Text("${item.stock}", color = WhiteText, fontSize = 14.sp, modifier = Modifier.weight(0.5f).padding(8.dp))
                    }
                }
            }
        }
    }

    if (editItem != null) {
        EditItemDialog(item = editItem!!, onDismiss = { editItem = null }, viewModel)
    }
}

@Composable
fun EditItemDialog(item: Item, onDismiss: () -> Unit, viewModel: InventoryViewModel) {
    var updatedStock by remember { mutableStateOf(item.stock.toString()) }
    var updatedPrice by remember { mutableStateOf(item.price.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f).background(DarkBackground),
            colors = CardDefaults.cardColors(containerColor = DarkBackground)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Item", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WhiteText)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = updatedPrice,
                    onValueChange = { updatedPrice = it },
                    label = { Text("Price", color = WhiteText) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = WhiteText)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = updatedStock,
                    onValueChange = { updatedStock = it },
                    label = { Text("Stock", color = WhiteText) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = WhiteText)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.updateItem(item.copy(price = updatedPrice.toDouble(), stock = updatedStock.toInt()))
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Update", color = WhiteText)
                    }
                    Button(
                        onClick = {
                            viewModel.deleteItem(item)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Delete", color = WhiteText)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, viewModel: InventoryViewModel) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemStock by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f).background(DarkBackground),
            colors = CardDefaults.cardColors(containerColor = DarkBackground)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add New Item", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WhiteText)
                Spacer(modifier = Modifier.height(8.dp))

                val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = WhiteText,
                    unfocusedBorderColor = WhiteText,
                    cursorColor = WhiteText
                )

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name", color = WhiteText) },
                    textStyle = TextStyle(color = WhiteText),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = { itemPrice = it },
                    label = { Text("Price", color = WhiteText) },
                    textStyle = TextStyle(color = WhiteText),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = itemStock,
                    onValueChange = { itemStock = it },
                    label = { Text("Stock", color = WhiteText) },
                    textStyle = TextStyle(color = WhiteText),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (itemName.isNotBlank() && itemPrice.isNotBlank() && itemStock.isNotBlank()) {
                            viewModel.addItem(Item(
                                id = System.currentTimeMillis().toInt(),
                                name = itemName,
                                stock = itemStock.toInt(),
                                price = itemPrice.toDouble()
                            ))
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Add Item", color = WhiteText)
                }
            }
        }
    }
}
