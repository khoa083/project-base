# Project Base

A robust Android project template utilizing modern Android development practices and architecture.

## 🛠 Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **Minimum SDK**: 29 (Android 10)
*   **Target SDK**: 36
*   **Java Version**: 17
*   **Gradle Version**: 9.0
*   **Build System**: Gradle Kotlin DSL (`.kts`)
*   **Architecture**: MVVM (Model-View-ViewModel)

## 🚀 Deployment & Setup

### Prerequisites
*   **Android Studio** (Latest Stable version recommended)
*   **JDK 17** must be installed and configured as the Gradle JDK.

## 📖 Usage Examples

This project uses a robust Base Architecture. Below are standard implementation guides for the core base classes.

### BaseFragment
Used for all Fragments to standardize DataBinding and ViewModel initialization.

```kotlin
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    // 1. Define Layout ID
    override val layoutId: Int = R.layout.fragment_home
    
    // 2. Initialize ViewModel (or use Koin/Hilt injection)
    override val mViewModel: HomeViewModel by viewModels()

    // 3. Setup Views (called after onViewCreated)
    override fun setupView(fragmentBinding: FragmentHomeBinding) {
        fragmentBinding.btnAction.setOnClickListener {
            mViewModel.doSomething()
        }
        
        // Observe UiState from BaseViewModel
        lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                // handle loading, error, success
            }
        }
    }
}
```


