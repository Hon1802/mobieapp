package hcmute.edu.vn.finalprojectdemo.singleton

object ResultHolder {
    var results: String = ""
    private var listener: ResultUpdateListener? = null

    fun setListener(listener: ResultUpdateListener) {
        this.listener = listener
    }

    fun updateResults(newResults: String) {
        results = newResults
        listener?.onResultUpdated(results)
    }
}interface ResultUpdateListener {
    fun onResultUpdated(results: String)
}