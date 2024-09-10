package io.github.landerlyoung.jennysample

class ComputeClass {

    external fun init(): Boolean

    external fun release()

    external fun setParam(globalHttpParam: Map<String, String>)

    external fun getGlobalParam(): Map<String, String>

    external fun request(json: String, listener: RequestListener): Boolean
}