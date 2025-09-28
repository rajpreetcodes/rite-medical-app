package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class Address(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    navController: NavController,
    onAddressSelected: (Address) -> Unit = {}
) {
    // Sample addresses
    val addresses = remember {
        mutableStateListOf(
            Address(
                id = "1",
                name = "John Doe",
                phone = "+1 234-567-8900",
                addressLine1 = "123 Main Street",
                addressLine2 = "Apt 4B",
                city = "New York",
                state = "NY",
                zipCode = "10001",
                isDefault = true
            ),
            Address(
                id = "2",
                name = "John Doe",
                phone = "+1 234-567-8900",
                addressLine1 = "456 Oak Avenue",
                addressLine2 = "Suite 200",
                city = "New York",
                state = "NY",
                zipCode = "10002",
                isDefault = false
            )
        )
    }
    
    var selectedAddressId by remember { mutableStateOf(addresses.find { it.isDefault }?.id ?: "") }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Address", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Current addresses
            addresses.forEach { address ->
                AddressCard(
                    address = address,
                    isSelected = address.id == selectedAddressId,
                    onSelect = { 
                        selectedAddressId = address.id
                        onAddressSelected(address)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Add new address button
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddAddressDialog = true },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Add Address",
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add New Address",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF007AFF)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Confirm button
            Button(
                onClick = {
                    val selectedAddress = addresses.find { it.id == selectedAddressId }
                    selectedAddress?.let { onAddressSelected(it) }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = selectedAddressId.isNotEmpty()
            ) {
                Text(
                    text = "Confirm Address",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    
    // Add address dialog
    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onAddAddress = { newAddress ->
                addresses.add(newAddress.copy(id = System.currentTimeMillis().toString()))
                showAddAddressDialog = false
            }
        )
    }
}

@Composable
fun AddressCard(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = address.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(address.addressLine1)
                            if (address.addressLine2.isNotEmpty()) {
                                append(", ${address.addressLine2}")
                            }
                            append(", ${address.city}, ${address.state} ${address.zipCode}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Default Address",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onAddAddress: (Address) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    
    val isFormValid = name.isNotEmpty() && phone.isNotEmpty() && 
                     addressLine1.isNotEmpty() && city.isNotEmpty() && 
                     state.isNotEmpty() && zipCode.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Address Line 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2 (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("ZIP Code") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFormValid) {
                        onAddAddress(
                            Address(
                                name = name,
                                phone = phone,
                                addressLine1 = addressLine1,
                                addressLine2 = addressLine2,
                                city = city,
                                state = state,
                                zipCode = zipCode,
                                isDefault = false
                            )
                        )
                    }
                },
                enabled = isFormValid
            ) {
                Text("Add Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddressScreenPreview() {
    AddressScreen(navController = rememberNavController())
}
