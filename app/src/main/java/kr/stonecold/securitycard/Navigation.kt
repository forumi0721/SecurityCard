package kr.stonecold.securitycard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kr.stonecold.securitycard.ui.main.MainScreen
import kr.stonecold.securitycard.ui.login.LoginScreen
import kr.stonecold.securitycard.ui.edit.EditScreen
import kr.stonecold.securitycard.ui.view.ViewScreen
import kr.stonecold.securitycard.View

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(onLoginSuccess = {
             backStack.removeAll { true }
             backStack.add(Main) 
          })
        }
        entry<Main> {
          MainScreen(onItemClick = { navKey -> backStack.add(navKey) }, onAddClick = { backStack.add(Edit()) }, onSettingsClick = { backStack.add(Settings) }, modifier = Modifier.safeDrawingPadding().padding(16.dp))
        }
        entry<Edit> { navKey ->
          EditScreen(id = navKey.id, onNavigateBack = { backStack.removeLast() })
        }
        entry<View> { navKey ->
          ViewScreen(id = navKey.id, onNavigateBack = { backStack.removeLast() }, onNavigateToEdit = { backStack.add(Edit(navKey.id)) })
        }
        entry<Settings> {
          kr.stonecold.securitycard.ui.settings.SettingsScreen(onNavigateBack = { backStack.removeLast() })
        }
      },
  )
}
