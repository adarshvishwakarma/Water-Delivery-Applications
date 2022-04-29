package models

import java.util.*

data class OrderedItemDetail(var ordered_items: ArrayList<String>? = null,
                        var ordered_shop_name: String? = null,
                        var ordered_time: String? = null,
                        var total_amount: String? = null,
                        var ordered_shop_spotimage: String? = null)


