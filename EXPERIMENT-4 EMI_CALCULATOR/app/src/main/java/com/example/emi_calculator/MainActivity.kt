package com.example.emi_calculator

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import kotlin.math.pow


data class Option(
    val loanAmount: String,
    val interestRate: Float,
    val tenure: Float,
    val emi: Double,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            EMICalculatorApp()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "emi_reminder", "EMI Reminder", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun EMICalculatorApp() {
    val context = LocalContext.current

    var loanAmount by remember { mutableStateOf("500000") }
    var interestRate by remember { mutableFloatStateOf(7.5f) }
    var tenure by remember { mutableFloatStateOf(12f) }
    var emi by remember { mutableDoubleStateOf(0.0) }
    var dueDate by remember { mutableStateOf("Select Due Date") }

    val compareOptions = remember { mutableStateListOf<Option>() }

    LaunchedEffect(Unit) {
        compareOptions.addAll(loadOptions(context))
    }

    var showCompareScreen by remember { mutableStateOf(false) }

    LaunchedEffect(loanAmount, interestRate, tenure) {
        emi = calculateEMI(loanAmount.toDoubleOrNull() ?: 0.0, interestRate, tenure.toInt())
    }

    Surface(color = Color(0xFFF5F5F5), modifier = Modifier.fillMaxSize()) {
        Box {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "EMI Calculator",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp), // Adjusted to fit all input fields
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.Gray),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Loan Amount Field
                        OutlinedTextField(
                            value = loanAmount,
                            onValueChange = { loanAmount = it },
                            label = { Text("Loan Amount (₹)", color = Color.Black) },
                            textStyle = TextStyle(color = Color.Black, fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = Color.Blue
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        // Interest Rate Field
                        OutlinedTextField(
                            value = interestRate.toString(),
                            onValueChange = { interestRate = it.toFloatOrNull() ?: 0f },
                            label = { Text("Interest Rate (%)", color = Color.Black) },
                            textStyle = TextStyle(color = Color.Black, fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = Color.Blue
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        // Tenure Field
                        OutlinedTextField(
                            value = tenure.toString(),
                            onValueChange = { tenure = it.toFloatOrNull() ?: 0f },
                            label = { Text("Loan Tenure (Months)", color = Color.Black) },
                            textStyle = TextStyle(color = Color.Black, fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = Color.Blue
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date", color = Color.Black) },
                            textStyle = TextStyle(color = Color.Black, fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = Color.Blue
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker(context) { selectedDate -> dueDate = selectedDate } }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Select Date",
                                        tint = Color.Black
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // EMI Result Card
                Card(
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    ),
                    border = BorderStroke(2.dp, Color.Gray),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Your Monthly EMI",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "₹%.2f".format(emi),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Option Button
                OutlinedButton(
                    onClick = {
                        val option = Option(
                            loanAmount = loanAmount,
                            interestRate = interestRate,
                            tenure = tenure,
                            emi = emi,
                            date = dueDate
                        )
                        compareOptions.add(option)
                        saveOptions(context, compareOptions)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(2.dp, Color.Gray),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Add Option", fontSize = 18.sp)
                }

                // Compare Options Button
                OutlinedButton(
                    onClick = { showCompareScreen = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(2.dp, Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Compare Options", fontSize = 18.sp)
                }
            }

            // Comparison Screen
            if (showCompareScreen) {
                CompareOptionsScreen(
                    options = compareOptions,
                    onOptionSelected = { selectedOption ->
                        loanAmount = selectedOption.loanAmount
                        interestRate = selectedOption.interestRate
                        tenure = selectedOption.tenure
                        emi = selectedOption.emi
                        dueDate = selectedOption.date
                        showCompareScreen = false
                    },
                    onClose = { showCompareScreen = false },
                    onDeleteAll = {
                        compareOptions.clear()
                        saveOptions(context, compareOptions)
                    }
                )
            }
        }
    }
}

@Composable
fun CompareOptionsScreen(
    options: List<Option>,
    onOptionSelected: (Option) -> Unit,
    onClose: () -> Unit,
    onDeleteAll: () -> Unit
) {
    Surface(color = Color(0xCCFFFFFF), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "EMI Comparison",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TableCell(text = "Option", weight = 1f)
                TableCell(text = "Loan Amt", weight = 1f)
                TableCell(text = "Rate(%)", weight = 1f)
                TableCell(text = "Tenure", weight = 1f)
                TableCell(text = "EMI (₹)", weight = 1f)
                TableCell(text = "Total (₹)", weight = 1f)
                TableCell(text = "Date", weight = 1f)
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Gray)

            options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 8.dp)
                ) {
                    TableCell(text = "Opt ${index + 1}", weight = 1f)
                    TableCell(text = option.loanAmount, weight = 1f)
                    TableCell(text = "${option.interestRate}", weight = 1f)
                    TableCell(text = "${option.tenure.toInt()}", weight = 1f)
                    TableCell(text = "₹%.2f".format(option.emi), weight = 1f)
                    TableCell(text = "₹%.2f".format(option.emi * option.tenure), weight = 1f)
                    TableCell(text = option.date, weight = 1f)
                }
                HorizontalDivider(color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = onClose,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(2.dp, Color.Gray)
                ) {
                    Text("Close", fontSize = 18.sp)
                }
                OutlinedButton(
                    onClick = onDeleteAll,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Red
                    ),
                    border = BorderStroke(2.dp, Color.Red)
                ) {
                    Text("Delete All", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.Black,
        modifier = Modifier
            .weight(weight)
            .padding(4.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )
}

fun calculateEMI(principal: Double, rate: Float, tenure: Int): Double {
    val monthlyRate = rate / (12 * 100)
    return if (tenure > 0 && principal > 0) {
        (principal * monthlyRate * (1 + monthlyRate).pow(tenure)) /
                ((1 + monthlyRate).pow(tenure) - 1)
    } else 0.0
}

fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        val formattedDate = "%02d/%02d/%d".format(selectedDay, selectedMonth + 1, selectedYear)
        onDateSelected(formattedDate)
    }, year, month, day)

    datePickerDialog.show()
}

fun saveOptions(context: Context, options: List<Option>) {
    val sharedPreferences = context.getSharedPreferences("emi_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val json = Gson().toJson(options)
    editor.putString("compare_options", json)
    editor.apply()
}

fun loadOptions(context: Context): List<Option> {
    val sharedPreferences = context.getSharedPreferences("emi_preferences", Context.MODE_PRIVATE)
    val json = sharedPreferences.getString("compare_options", null)
    val type = object : TypeToken<List<Option>>() {}.type
    return Gson().fromJson(json, type) ?: emptyList()
}