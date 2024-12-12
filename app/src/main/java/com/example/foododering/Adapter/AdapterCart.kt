import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foododering.databinding.PopularItemCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterCart(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemimages: MutableList<String>,
    private val cartItemprices: MutableList<String>,
    private val cartQuantity: MutableList<Int>,
    private val cartDescriptions: MutableList<String>
) : RecyclerView.Adapter<AdapterCart.PopularViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var cartItemsReference: DatabaseReference
    private var itemQuantities: IntArray

    init {
        val databaseReference = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid
        itemQuantities = IntArray(cartItems.size) { 1 }
        cartItemsReference =
            databaseReference.reference.child("users").child(userId ?: "").child("CartItems")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = PopularItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class PopularViewHolder(private val binding: PopularItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                tvFoodName.text = cartItems[position]
                tvPrice.text = cartItemprices[position]
                tvQuantity.text = cartQuantity[position].toString()
                val uriString = cartItemimages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(imgViewItem)

                btnMinus.setOnClickListener {
                    decreaseQuantity(position, binding)
                }

                btnPlus.setOnClickListener {
                    increaseQuantity(position, binding)
                }

                btnDelete.setOnClickListener {
                    deleteItem(position)
                }
            }
        }
    }

    private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
        cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var uniqueKey: String? = null
                snapshot.children.forEachIndexed { index, dataSnapshot ->
                    if (index == positionRetrieve) {
                        uniqueKey = dataSnapshot.key
                        return@forEachIndexed
                    }
                }
                onComplete(uniqueKey)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                onComplete(null)
            }
        })
    }

    fun deleteItem(position: Int) {
        // Kiểm tra nếu vị trí hợp lệ trong danh sách
        if (position in cartItems.indices) {
            // Nếu chỉ còn 0 sản phẩm trong giỏ hàng
            if (cartItems.size == 0) {
                // Xóa luôn sản phẩm ở vị trí 0
                getUniqueKeyAtPosition(position) { uniqueKey ->
                    if (uniqueKey != null) {
                        removeItem(position, uniqueKey)
                    } else {
                        Toast.makeText(context, "Không thể xác định sản phẩm", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Nếu còn nhiều hơn 0 sản phẩm, chỉ giảm số lượng
                getUniqueKeyAtPosition(position) { uniqueKey ->
                    if (uniqueKey != null) {
                        removeItem(position, uniqueKey)
                    } else {
                        Toast.makeText(context, "Không thể xác định sản phẩm", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Nếu vị trí không hợp lệ
            Toast.makeText(context, "Vị trí không hợp lệ: $position", Toast.LENGTH_SHORT).show()
            Log.e("AdapterCart", "Invalid position: $position, size: ${cartItems.size}")
        }
    }


    private fun removeItem(position: Int, uniqueKey: String) {
        cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
            if (position in cartItems.indices) {
                // Xóa mục khỏi danh sách
                cartItems.removeAt(position)
                cartItemimages.removeAt(position)
                cartItemprices.removeAt(position)
                cartQuantity.removeAt(position)
                cartDescriptions.removeAt(position)

                // Cập nhật lại mảng số lượng
                itemQuantities = itemQuantities.filterIndexed { index, _ ->
                    index != position
                }.toIntArray()

                // Sử dụng notifyDataSetChanged để tránh lỗi trạng thái không đồng nhất
                notifyDataSetChanged()
            } else {
                Log.e("AdapterCart", "Invalid position after Firebase update: $position")
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show()
        }
    }



    private fun increaseQuantity(position: Int, binding: PopularItemCartBinding) {
        if (position < cartQuantity.size) {
            cartQuantity[position]++
            binding.tvQuantity.text = cartQuantity[position].toString()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val foodName = cartItems[position]
                val itemRef = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userId)
                    .child("CartItems")
                    .child(foodName)

                itemRef.child("foodQuantity").setValue(cartQuantity[position])
                    .addOnSuccessListener {
                        // Thành công, không cần xử lý thêm
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Cập nhật số lượng thất bại", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun decreaseQuantity(position: Int, binding: PopularItemCartBinding) {
        if (position < cartQuantity.size && cartQuantity[position] > 1) {
            cartQuantity[position]--
            binding.tvQuantity.text = cartQuantity[position].toString()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val foodName = cartItems[position]
                val itemRef = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userId)
                    .child("CartItems")
                    .child(foodName)

                itemRef.child("foodQuantity").setValue(cartQuantity[position])
                    .addOnSuccessListener {
                        // Thành công, không cần xử lý thêm
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Cập nhật số lượng thất bại", Toast.LENGTH_SHORT).show()
                    }
            }
        } else if (position < cartQuantity.size && cartQuantity[position] <= 1) {
            deleteItem(position)
        }
    }

    fun getItemQuantities(): MutableList<Int> {
        return cartQuantity.toMutableList()
    }
}