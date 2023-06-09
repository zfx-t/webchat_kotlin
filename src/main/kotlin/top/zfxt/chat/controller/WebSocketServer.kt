package top.zfxt.chat.controller

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import jakarta.websocket.*
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import java.util.concurrent.ConcurrentHashMap

/**
 *  @author:zfxt
 *  @version:1.0
 */
@Controller
@ServerEndpoint("/chat/{username}")
class WebSocketServer {
    companion object{

        private  val log = LoggerFactory.getLogger(WebSocketServer.javaClass)

        /**
         * 记录当前在线人数
         */
        private val sessionMap:MutableMap<String,Session> = ConcurrentHashMap();
    }

    /**
     * 建立成功调用的方法
     */
    @OnOpen
    public fun onOpen(session:Session,@PathParam("username") username:String){
        sessionMap.put(username,session)
        log.info("有新用户加入，username={}，当前在线人数为：{}",username, sessionMap.size)
        var result: JSONObject = JSONObject()
        var array: JSONArray = JSONArray()
        result.set("users",array)
        for (key in sessionMap.keys){
            array.add(JSONObject().set("username",key))
        }
        sendAllMessage(JSONUtil.toJsonStr(result))
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public fun onClose(session: Session,@PathParam("username") username: String){
        sessionMap.minus(username)
        log.info("有一连接关闭，移除username={}的用户session，当前在线人数为：{}",username, sessionMap.size)
    }
    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public fun onMessage(message: String,@PathParam("username") username: String){
        log.info("服务器收到用户username={}的消息:{}",username,message)
        var obj = JSONUtil.parseObj(message)
        var toUsername = obj.getStr("to")
        var text = obj.getStr("text")
        var toSession = sessionMap.get(toUsername)
        if(toSession != null){
            var js = JSONObject().set("from",username).set("text",text).toString()
            sendMassage(js,toSession)
            log.info("发送给用户username={}，消息：{}",toUsername,js)
        }else{
            log.info("发送失败，位置啊到用户username={}的session",toUsername)
        }
    }
    @OnError
    public fun onError(session: Session,error:Throwable){
        log.error("发生错误")
        error.printStackTrace()
    }

    /**
     * 服务器发送消息给客户端
     */
    private fun sendMassage(message:String,toSession: Session){
        try {
            log.info("服务器给客户端[{}]发送消息{}",toSession.id,message)
            toSession.basicRemote.sendText(message)
        }catch (e:Exception){
            log.error("服务器发送消息给客户端失败",e)
        }
    }
    /**
     * 服务器发送消息给所有客户端
     */
    private fun sendAllMessage(message:String){
        try {
            for(session in sessionMap.values){
                log.info("服务端给客户端[{}]发送消息{}",session.id,message)
                session.basicRemote.sendText(message)
            }
        }catch (e:Exception){
            log.error("服务器发送消息给客户端失败",e)
        }
    }
}