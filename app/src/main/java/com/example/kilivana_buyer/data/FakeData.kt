package com.example.kilivana_buyer.data

import com.example.kilivana_buyer.R
import com.example.kilivana_buyer.model.*

object FakeData {
    val products = listOf(
        product("tomatoes", "Tomatoes", "Vegetables", 80, "kg", R.drawable.produce_tomatoes, "Mavuno Fresh Farm", "Thika"),
        product("sukuma_wiki", "Sukuma wiki", "Vegetables", 45, "bunch", R.drawable.produce_sukuma_wiki, "Green Valley Farm", "Kiambu"),
        product("spinach", "Spinach", "Vegetables", 60, "bunch", R.drawable.produce_spinach, "Wanjiku Greens", "Limuru"),
        product("cabbage", "Cabbage", "Vegetables", 70, "piece", R.drawable.produce_cabbage, "Rift Harvest", "Nakuru"),
        product("carrots", "Carrots", "Vegetables", 90, "kg", R.drawable.produce_carrots, "Mavuno Fresh Farm", "Thika"),
        product("onions", "Red onions", "Vegetables", 110, "kg", R.drawable.produce_onions, "Kijani Growers", "Naivasha"),
        product("bananas", "Bananas", "Fruits", 120, "kg", R.drawable.produce_bananas, "Njeri Family Farm", "Murang'a"),
        product("avocados", "Avocados", "Fruits", 160, "kg", R.drawable.produce_avocados, "Kiamwathi Orchards", "Nyeri"),
        product("mangoes", "Mangoes", "Fruits", 140, "kg", R.drawable.produce_mangoes, "Coastline Produce", "Mombasa"),
        product("maize", "Dry maize", "Cereals and Grains", 75, "kg", R.drawable.produce_maize, "Uasin Gishu Co-op", "Eldoret"),
        product("irish_potatoes", "Irish potatoes", "Tubers", 95, "kg", R.drawable.produce_irish_potatoes, "Rift Harvest", "Nakuru"),
        product("sweet_potatoes", "Sweet potatoes", "Tubers", 85, "kg", R.drawable.produce_sweet_potatoes, "Kijani Growers", "Naivasha"),
        product("green_beans", "Green beans", "Legumes", 130, "kg", R.drawable.produce_green_beans, "Wanjiku Greens", "Limuru"),
        product("beans", "Rose coco beans", "Legumes", 180, "kg", R.drawable.produce_beans, "Uasin Gishu Co-op", "Eldoret"),
        product("eggs", "Farm eggs", "Poultry and Eggs", 450, "tray", R.drawable.produce_eggs, "Highland Poultry", "Kiambu"),
        product("fresh_milk", "Fresh milk", "Dairy", 75, "litre", R.drawable.produce_fresh_milk, "Karibu Dairy", "Nyandarua")
    )
    private fun product(id:String,name:String,category:String,price:Int,unit:String,image:Int,seller:String,town:String)=Product(id,name,category,price,unit,"Fresh produce sourced directly from verified farmers. Packed carefully for delivery.",image,seller,town,if(id=="eggs") StockStatus.LowStock else StockStatus.InStock,1,4.7,36,"Harvested this week")
}
