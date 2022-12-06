package tma.inf.upol.loveckydenik.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tma.inf.upol.loveckydenik.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val uriString = intent.getStringExtra(MainActivity.IMAGE_URI_KEY)
        val uri = Uri.parse(uriString)
        uri?.let {
            binding.detailPhotoIv.setImageURI(uri)
        }

    }
}