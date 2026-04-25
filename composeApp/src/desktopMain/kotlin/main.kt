import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fakturkuid.app.App
import com.fakturkuid.app.di.appModule
import com.fakturkuid.app.di.platformModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule, platformModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Fakturku ID",
    ) {
        App()
    }
}
