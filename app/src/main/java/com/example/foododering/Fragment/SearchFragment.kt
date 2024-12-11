import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foododering.Adapter.AdapterMenu
import com.example.foododering.R
import com.example.foododering.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: AdapterMenu
    private val originFoodName = listOf("Hot Dog", "Sandwich", "Salad", "Spaghetti", "Bánh Mì", "Phở", "Bánh Giò", "Bún Thang", "Bún Đậu")
    private val originFoodPrice = listOf("5", "10", "8", "11", "2", "3", "4", "1", "2")
    private val originFoodImage = listOf(
        R.drawable.hotdog,
        R.drawable.sandwich,
        R.drawable.salad,
        R.drawable.spaghetti,
        R.drawable.banhmi,
        R.drawable.pho,
        R.drawable.banhgio,
        R.drawable.bunthang,
        R.drawable.bundau
    )
    private val filterFoodName = mutableListOf<String>()
    private val filterFoodImage = mutableListOf<Int>()
    private val filterFoodPrice = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        //adapter = AdapterMenu(filterFoodName, filterFoodPrice, filterFoodImage)
        binding.SearchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.SearchRecyclerView.adapter = adapter

        showAllMenu()
        setupSearchView()

        return binding.root
    }

    private fun showAllMenu() {
        filterFoodName.clear()
        filterFoodImage.clear()
        filterFoodPrice.clear()

        filterFoodName.addAll(originFoodName)
        filterFoodImage.addAll(originFoodImage)
        filterFoodPrice.addAll(originFoodPrice)
        adapter.notifyDataSetChanged()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterItems(newText)
                return true
            }
        })
    }

    private fun filterItems(query: String) {
        filterFoodName.clear()
        filterFoodImage.clear()
        filterFoodPrice.clear()

        originFoodName.forEachIndexed { index, name ->
            if (name.contains(query, ignoreCase = true)) {
                filterFoodName.add(name)
                filterFoodImage.add(originFoodImage[index])
                filterFoodPrice.add(originFoodPrice[index])
            }
        }
        adapter.notifyDataSetChanged()
    }
}
