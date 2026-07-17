package ucenfotec.ac.cr.flydevs.domain.model

enum class UserRole {
    USER,
    DELIVERY,
    SELLER,
    BUYER,
    ADMIN;

    companion object{
        fun fromFirestore(value:String?): UserRole{
            return when(value){
                "USER" -> USER
                "DELIVERY" -> DELIVERY
                "ADMIN" -> ADMIN
                "BUYER" -> BUYER
                else -> USER // Default role if value is null or unrecognized
            }
        }
    }
}