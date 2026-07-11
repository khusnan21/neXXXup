package com.unan.nexxxup

import android.content.Context
import com.unan.api.setContext
import com.unan.nexxxup.utils.DataStore.getKey
import com.unan.nexxxup.utils.DataStore.removeKeys
import com.unan.nexxxup.utils.DataStore.setKey
import java.lang.ref.WeakReference

/**
 * Deprecated alias for NexxxupApp for backwards compatibility with plugins.
 * Use NexxxupApp instead.
 */
// Deprecate after next stable
/*@Deprecated(
    message = "AcraApplication is deprecated, use NexxxupApp instead",
    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp"),
    level = DeprecationLevel.WARNING
)*/
class AcraApplication {
	// All methods here can be changed to be a wrapper around Nexxxup app
	// without a seperate deprecation after next stable. All methods should
	// also be deprecated at that time.
	companion object {

		// This can be removed without deprecation after next stable
		private var _context: WeakReference<Context>? = null
		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.context"),
		    level = DeprecationLevel.WARNING
		)*/
		var context
		get() = _context?.get()
		internal set(value) {
			_context = WeakReference(value)
			setContext(WeakReference(value))
		}

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.removeKeys(folder)"),
		    level = DeprecationLevel.WARNING
		)*/
		fun removeKeys(folder: String): Int? {
            return context?.removeKeys(folder)
        }

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.setKey(path, value)"),
		    level = DeprecationLevel.WARNING
		)*/
		fun <T> setKey(path: String, value: T) {
			context?.setKey(path, value)
		}

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.setKey(folder, path, value)"),
		    level = DeprecationLevel.WARNING
		)*/
		fun <T> setKey(folder: String, path: String, value: T) {
			context?.setKey(folder, path, value)
		}

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.getKey(path, defVal)"),
		    level = DeprecationLevel.WARNING
		)*/
		inline fun <reified T : Any> getKey(path: String, defVal: T?): T? {
			return context?.getKey(path, defVal)
		}

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.getKey(path)"),
		    level = DeprecationLevel.WARNING
		)*/
		inline fun <reified T : Any> getKey(path: String): T? {
			return context?.getKey(path)
		}

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.getKey(folder, path)"),
		    level = DeprecationLevel.WARNING
		)*/
		inline fun <reified T : Any> getKey(folder: String, path: String): T? {
			return context?.getKey(folder, path)
		}

		/*@Deprecated(
		    message = "AcraApplication is deprecated, use NexxxupApp instead",
		    replaceWith = ReplaceWith("com.unan.nexxxup.NexxxupApp.getKey(folder, path, defVal)"),
		    level = DeprecationLevel.WARNING
		)*/
		inline fun <reified T : Any> getKey(folder: String, path: String, defVal: T?): T? {
			return context?.getKey(folder, path, defVal)
		}
	}
}
