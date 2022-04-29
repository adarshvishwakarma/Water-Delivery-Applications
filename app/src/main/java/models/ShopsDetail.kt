package models

data class ShopsDetail(var shop_name: String? = null,
                       var shop_spotimage: String? = null,
                       var shop_prep_time: String? = null,
                       var average_price: String? = null,
                       var shop_uid: String? = null,
                       var shop_phonenumber: String? = null,
                       var latitude: Double? = null,
                       var longitude: Double? = null)