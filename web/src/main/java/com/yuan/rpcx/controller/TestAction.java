package com.yuan.rpcx.controller;

import com.yuan.rpcx.Api.RpcxCall;
import com.yuan.rpcx.Enum.ReqMedType;
import entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * TestAction
 *
 * @author yuanqing
 * @create 2018-10-06 上午8:09
 **/
@Controller
public class TestAction {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("simpleRpcxCall")
    private RpcxCall rpcxCall;

    @RequestMapping(value={"getPerson"}, method = RequestMethod.GET)
    @ResponseBody
    public Person getPerson(){

        Person person = new Person();
        person.setId("8888");
        person.setName("yuanqing");

        logger.info("getPerson, person={}",person);
//        System.out.println("getPerson");

        return person;
    }

    @RequestMapping(value={"testRpcx"}, method = RequestMethod.GET)
    @ResponseBody
    public String testRpcx() throws Exception{
        logger.info("testRpcx");
        String result = null;
        try{
            Map<String,String> params = new HashMap<>();
            params.put("Id","1111");
            result = rpcxCall.callForTest("StudentSrv","GetStudentById",params, ReqMedType.POST);
        }catch (Exception ex){
            logger.error(ex.getMessage(),ex);
            throw ex;
        }
        return result;
    }
}
