package com.billybobbain.wellnest

sealed class Screen(val route: String) {
    object ProfileSelection : Screen("profile_selection")
    object Main : Screen("main")
    object Medications : Screen("medications")
    object AddEditMedication : Screen("add_edit_medication/{medicationId}") {
        fun createRoute(medicationId: Long? = null) =
            "add_edit_medication/${medicationId ?: "new"}"
    }
    object Appointments : Screen("appointments")
    object AddEditAppointment : Screen("add_edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: Long? = null) =
            "add_edit_appointment/${appointmentId ?: "new"}"
    }
    object Contacts : Screen("contacts")
    object AddEditContact : Screen("add_edit_contact/{contactId}") {
        fun createRoute(contactId: Long? = null) =
            "add_edit_contact/${contactId ?: "new"}"
    }
    object HealthProfile : Screen("health_profile")
    object Insurance : Screen("insurance")
    object AddEditInsurancePolicy : Screen("add_edit_insurance/{policyId}") {
        fun createRoute(policyId: Long? = null) =
            "add_edit_insurance/${policyId ?: "new"}"
    }
    object SecurityCodes : Screen("security_codes")
    object AddEditSecurityCode : Screen("add_edit_security/{codeId}") {
        fun createRoute(codeId: Long? = null) =
            "add_edit_security/${codeId ?: "new"}"
    }
    object Settings : Screen("settings")
    object AddEditProfile : Screen("add_edit_profile/{profileId}") {
        fun createRoute(profileId: Long? = null) =
            "add_edit_profile/${profileId ?: "new"}"
    }
}
