package com.billybobbain.wellnest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.billybobbain.wellnest.ui.screens.*

@Composable
fun WellnestApp(viewModel: WellnestViewModel) {
    val navController = rememberNavController()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()

    // Determine start destination
    val startDestination = if (selectedProfileId == null && allProfiles.isEmpty()) {
        Screen.ProfileSelection.route
    } else if (selectedProfileId == null) {
        Screen.ProfileSelection.route
    } else {
        Screen.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.ProfileSelection.route) {
            ProfileSelectionScreen(
                viewModel = viewModel,
                onProfileSelected = { profileId ->
                    viewModel.selectProfile(profileId)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                    }
                },
                onAddProfile = {
                    navController.navigate(Screen.AddEditProfile.createRoute())
                },
                onEditProfile = { profileId ->
                    navController.navigate(Screen.AddEditProfile.createRoute(profileId))
                }
            )
        }

        composable(
            route = Screen.AddEditProfile.route,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileIdArg = backStackEntry.arguments?.getString("profileId")
            val profileId = if (profileIdArg == "new") null else profileIdArg?.toLongOrNull()

            AddEditProfileScreen(
                viewModel = viewModel,
                profileId = profileId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToMedications = { navController.navigate(Screen.Medications.route) },
                onNavigateToAppointments = { navController.navigate(Screen.Appointments.route) },
                onNavigateToLocations = { navController.navigate(Screen.Locations.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToHealthProfile = { navController.navigate(Screen.HealthProfile.route) },
                onNavigateToInsurance = { navController.navigate(Screen.Insurance.route) },
                onNavigateToDoctors = { navController.navigate(Screen.Doctors.route) },
                onNavigateToRoomInfo = { navController.navigate(Screen.RoomInfo.route) },
                onNavigateToSupplies = { navController.navigate(Screen.Supplies.route) },
                onNavigateToMessages = { navController.navigate(Screen.Messages.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onSwitchProfile = {
                    navController.navigate(Screen.ProfileSelection.route)
                }
            )
        }

        composable(Screen.Medications.route) {
            MedicationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddMedication = { navController.navigate(Screen.AddEditMedication.createRoute()) },
                onEditMedication = { id -> navController.navigate(Screen.AddEditMedication.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditMedication.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val medicationIdArg = backStackEntry.arguments?.getString("medicationId")
            val medicationId = if (medicationIdArg == "new") null else medicationIdArg?.toLongOrNull()

            AddEditMedicationScreen(
                viewModel = viewModel,
                medicationId = medicationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Appointments.route) {
            AppointmentsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddAppointment = { navController.navigate(Screen.AddEditAppointment.createRoute()) },
                onEditAppointment = { id -> navController.navigate(Screen.AddEditAppointment.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditAppointment.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentIdArg = backStackEntry.arguments?.getString("appointmentId")
            val appointmentId = if (appointmentIdArg == "new") null else appointmentIdArg?.toLongOrNull()

            AddEditAppointmentScreen(
                viewModel = viewModel,
                appointmentId = appointmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddContact = { navController.navigate(Screen.AddEditContact.createRoute()) },
                onEditContact = { id -> navController.navigate(Screen.AddEditContact.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditContact.route,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactIdArg = backStackEntry.arguments?.getString("contactId")
            val contactId = if (contactIdArg == "new") null else contactIdArg?.toLongOrNull()

            AddEditContactScreen(
                viewModel = viewModel,
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HealthProfile.route) {
            HealthProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Insurance.route) {
            InsuranceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddPolicy = { navController.navigate(Screen.AddEditInsurancePolicy.createRoute()) },
                onEditPolicy = { id -> navController.navigate(Screen.AddEditInsurancePolicy.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditInsurancePolicy.route,
            arguments = listOf(navArgument("policyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val policyIdArg = backStackEntry.arguments?.getString("policyId")
            val policyId = if (policyIdArg == "new") null else policyIdArg?.toLongOrNull()

            AddEditInsurancePolicyScreen(
                viewModel = viewModel,
                policyId = policyId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SecurityCodes.route) {
            SecurityCodesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddCode = { navController.navigate(Screen.AddEditSecurityCode.createRoute()) },
                onEditCode = { id -> navController.navigate(Screen.AddEditSecurityCode.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditSecurityCode.route,
            arguments = listOf(navArgument("codeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val codeIdArg = backStackEntry.arguments?.getString("codeId")
            val codeId = if (codeIdArg == "new") null else codeIdArg?.toLongOrNull()

            AddEditSecurityCodeScreen(
                viewModel = viewModel,
                codeId = codeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RoomInfo.route) {
            RoomInfoScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Supplies.route) {
            SuppliesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddSupply = { navController.navigate(Screen.AddEditSupply.createRoute()) },
                onEditSupply = { id -> navController.navigate(Screen.AddEditSupply.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditSupply.route,
            arguments = listOf(navArgument("supplyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val supplyIdArg = backStackEntry.arguments?.getString("supplyId")
            val supplyId = if (supplyIdArg == "new") null else supplyIdArg?.toLongOrNull()

            AddEditSupplyScreen(
                viewModel = viewModel,
                supplyId = supplyId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Messages.route) {
            MessagesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddMessage = { navController.navigate(Screen.AddEditMessage.createRoute()) },
                onEditMessage = { id -> navController.navigate(Screen.AddEditMessage.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditMessage.route,
            arguments = listOf(navArgument("messageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val messageIdArg = backStackEntry.arguments?.getString("messageId")
            val messageId = if (messageIdArg == "new") null else messageIdArg?.toLongOrNull()

            AddEditMessageScreen(
                viewModel = viewModel,
                messageId = messageId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Locations.route) {
            LocationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddLocation = { navController.navigate(Screen.AddEditLocation.createRoute()) },
                onEditLocation = { id -> navController.navigate(Screen.AddEditLocation.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditLocation.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationIdArg = backStackEntry.arguments?.getString("locationId")
            val locationId = if (locationIdArg == "new") null else locationIdArg?.toLongOrNull()

            AddEditLocationScreen(
                viewModel = viewModel,
                locationId = locationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Doctors.route) {
            DoctorsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddDoctor = { navController.navigate(Screen.AddEditDoctor.createRoute()) },
                onEditDoctor = { id -> navController.navigate(Screen.AddEditDoctor.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditDoctor.route,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorIdArg = backStackEntry.arguments?.getString("doctorId")
            val doctorId = if (doctorIdArg == "new") null else doctorIdArg?.toLongOrNull()

            AddEditDoctorScreen(
                viewModel = viewModel,
                doctorId = doctorId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
