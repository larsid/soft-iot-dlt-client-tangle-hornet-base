package br.uefs.larsid.iot.soft.model;

import br.uefs.larsid.iot.soft.services.IService;

public class Service implements IService {
    
    @Override
    public String hello(String name) {
        return "Hello"+ name +"!";
    }
    
}