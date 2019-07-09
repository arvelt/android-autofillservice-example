package example.com.myautofill

import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.autofill.AutofillValue
import android.widget.RemoteViews

class MyAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        val emailFields = mutableListOf<AssistStructure.ViewNode>()
        identifyEmailFields(structure.getWindowNodeAt(0).getRootViewNode(), emailFields)

        // Do nothing if no email fields found
        if (emailFields.size == 0)
            return
        val emailField = emailFields[0]

        // Fetch user data that matches the fields.
        val username = "My name"
        val password = "My email"

        // Build the presentation of the datasets
        val usernamePresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        usernamePresentation.setTextViewText(android.R.id.text1, "my_username")
        val passwordPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        passwordPresentation.setTextViewText(android.R.id.text1, "Password for my_username")

        // Add a dataset to the response
        val fillResponse: FillResponse = FillResponse.Builder()
            .addDataset(
                Dataset.Builder()
                    .setValue(
                        emailField.autofillId,
                        AutofillValue.forText(username),
                        usernamePresentation
                    )
                    .setValue(
                        emailField.autofillId,
                        AutofillValue.forText(password),
                        passwordPresentation
                    )
                    .build()
            )
            .build()

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log.d("onFillRequest", request.toString())
    }

    private fun identifyEmailFields(
        node: AssistStructure.ViewNode,
        emailFields: MutableList<AssistStructure.ViewNode>
    ) {
        if (node.className.contains("EditText")) {

            // check view id
            val viewId = node.idEntry
            if (viewId != null && (viewId.contains("email") || viewId.contains("username"))) {
                emailFields.add(node)
                return
            }

            // check autofillHint
            val hints = (node.autofillHints ?: arrayOf()).toList()
            for (hint in hints) {
                if (viewId.contains("email") || viewId.contains("username")) {
                    emailFields.add(node)
                    return
                }
            }
        }
        for (i in 0 until node.childCount) {
            identifyEmailFields(node.getChildAt(i), emailFields)
        }
    }
}
