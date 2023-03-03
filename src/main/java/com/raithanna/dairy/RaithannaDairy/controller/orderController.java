package com.raithanna.dairy.RaithannaDairy.controller;

import com.raithanna.dairy.RaithannaDairy.models.customer;
import com.raithanna.dairy.RaithannaDairy.models.dailySales;
import com.raithanna.dairy.RaithannaDairy.models.productMaster;
import com.raithanna.dairy.RaithannaDairy.models.saleOrder;
import com.raithanna.dairy.RaithannaDairy.repositories.CustomerRepository;
import com.raithanna.dairy.RaithannaDairy.repositories.DailySalesRepository;
import com.raithanna.dairy.RaithannaDairy.repositories.ProductMasterRepository;
import com.raithanna.dairy.RaithannaDairy.repositories.SaleOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class orderController {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductMasterRepository productMasterRepository;

    @Autowired
    private DailySalesRepository dailySalesRepository;

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @RequestMapping("/createOrder")
    public String createOrder_html(Model model, HttpSession session) {
        if (session.getAttribute("loggedIn").equals("yes")) {

            // customer info from login


            // order no -- 90
            // 91  -- 10 item  --
            // 91  -- 5 items  -- 91 created

/*
            Iterable<dailySales> dailysales = dailySalesRepository.findAll();
            List<Integer> orderNos = new ArrayList<>();
            List<customer> Customers = new ArrayList<>();
            for (dailySales i : dailysales) {
                orderNos.add(i.getOrderNo());
            }
            System.out.println(dailysales);
            //int orderNo = 1;
            if (!orderNos.isEmpty()) {
                //orderNo = Integer.parseInt(String.valueOf(orderNos.get(orderNos.size() - 1))) + 1;
                orderNo = 111;
            }
            */

            //int orderNo = saleOrderRepository.max();

            saleOrder saleOrder =   saleOrderRepository.findTopByOrderByOrderNoDesc();
            Integer orderNo;
            if(saleOrder == null)
            {
                orderNo =1;
            } else{
                orderNo = saleOrder.getOrderNo()+1;
            }

            Iterable<customer> CustomersIterable = customerRepository.findAll();
            List<customer> Customers = new ArrayList<>();
            for (customer Customer : CustomersIterable) {
                Customers.add(Customer);
            }

            Iterable<productMaster> Products = productMasterRepository.findBySplCustCode(Customers.get(0).getCode().toString());
            int size = 0;
            for (productMaster value : Products) {
                size++;
            }
            if (size == 0) {
                Products = productMasterRepository.findBySplCustCode("");
            }
            //LocalDate date =LocalDate.of("int year";"int month","int dayOfMonth");
            //LocalDate date =LocalDate.of(2023,2,28);
            model.addAttribute("customers", Customers);
            model.addAttribute("products", Products);
            model.addAttribute("orderNo", orderNo);
            //model.addAttribute("dateTime", date);
            return "createOrder";
        }
        List messages = new ArrayList<>();
        messages.add("Login First");
        model.addAttribute("messages", messages);
        return "redirect:/login";
    }

    @PostMapping("/getproductValues")
    public ResponseEntity<?> getproductRatesByCustcode(@RequestParam Map<String, String> body, Model model) {
        System.out.println(body);
        productMaster product = productMasterRepository.findBySplCustCodeAndPCode(body.get("code"), body.get("pcode"));
        productMaster commonproduct = productMasterRepository.findBySplCustCodeAndPCode("", body.get("pcode"));
        if (product == null) {
            product = commonproduct;
        }
        System.out.println(product);
        model.addAttribute("products", product);
        Map<String, String> respBody = new HashMap<>();
        respBody.putIfAbsent("specialValue",String.valueOf(product.getUnitRate() - Integer.parseInt(body.get("disc"))));
        respBody.putIfAbsent("commonValue", String.valueOf(commonproduct.getUnitRate()));
        respBody.putIfAbsent("amount", String.valueOf((product.getUnitRate() * Integer.parseInt(body.get("qty")))));
        respBody.putIfAbsent("ttlValue", String.valueOf((product.getUnitRate() - Integer.parseInt(body.get("disc"))) * Integer.parseInt(body.get("qty"))));
        return ResponseEntity.ok(respBody);

    }

    @PostMapping("/createOrder")
    public String createOrder(Model model, @ModelAttribute dailySales order, @RequestParam Map<String, String> orderDetails, HttpServletRequest request, HttpSession session) throws InterruptedException {
        List<String> messages = new ArrayList<>();
        System.out.println(orderDetails);

        try {
            // daily sales table save
            order.setOrderNo(Integer.parseInt(orderDetails.get("orderDetails[orderNo]")));
            order.setCustCode(orderDetails.get("orderDetails[custCode]"));
            order.setDisc(Double.parseDouble(orderDetails.get("orderDetails[disc]")));
            order.setNetAmount(Double.parseDouble(orderDetails.get("orderDetails[netAmount]")));
            order.setAmount(Double.parseDouble(orderDetails.get("orderDetails[amount]")));
            order.setProdCode(orderDetails.get("orderDetails[prodCode]"));
            order.setQuantity(Double.parseDouble(orderDetails.get("orderDetails[quantity]")));
            order.setUnitRate(Double.parseDouble(orderDetails.get("orderDetails[unitRate]")));
            dailySalesRepository.save(order);

        } catch (Exception handlerException) {
            messages.add("Error Creating the order pls retry");
            model.addAttribute("messages", messages);
            return "/createOrder";
        }
        model.addAttribute("messages", messages);
        return "redirect:/";
    }

    @PostMapping("/createSaleOrder")
    public String createSaleOrder(@RequestParam String orderNo) {

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+orderNo);
        List<dailySales> orderProducts = dailySalesRepository.findByOrderNo(Integer.parseInt(orderNo));

        while (true) {
            if (orderProducts.isEmpty()) {
                orderProducts = dailySalesRepository.findByOrderNo(Integer.parseInt(orderNo));
            } else {
                break;
            }
        }


        System.out.println(orderProducts.size());
        System.out.println("$$$$$$$$$$$$$$$");
        System.out.println(orderProducts.size());
        System.out.println("$$$$$$$$$$$$$$$");
        double amt = 0;
        double netamt = 0;
        double discamt = 0;
        String custCode = "";

        for (dailySales product : orderProducts) {
            amt = amt + product.getAmount();
            netamt = netamt + product.getNetAmount();
            //discamt = discamt + product.getDisc();
            //custCode = product.getCustCode();
            System.out.println("amt" +amt);
            System.out.println("netamt" +netamt);
        }

        saleOrder saleorder = new saleOrder();
        saleorder.setOrderNo(Integer.parseInt(orderNo));
        saleorder.setAmount(amt);
        saleorder.setNetAmount(netamt);
        saleorder.setDisc(amt - netamt);
        saleorder.setCustCode(custCode);

        System.out.println(saleorder);
        saleOrderRepository.save(saleorder);
        return "redirect:/";
    }

    @GetMapping("/getOrder")
    public String getOrder(@RequestParam(name = "orderNo", defaultValue = "1") Integer orderNo, Model model) {
        List<dailySales> orderProducts = dailySalesRepository.findByOrderNo(orderNo);
           saleOrder sale_order = saleOrderRepository.findByOrderNo(orderNo);

        model.addAttribute("orderProducts", orderProducts);
        model.addAttribute("sale_order", sale_order);
        return "orderDisplay";
    }

    @GetMapping("/order/view/{orderNo}")
    public String viewOrderForm(Model model, @PathVariable(name = "orderNo") Integer orderNo, HttpSession session) {
        if (session.getAttribute("loggedIn").equals("yes")) {
            List<customer> Customers = new ArrayList<>();
            Iterable<customer> CustomersIterable = customerRepository.findAll();
            for (customer Customer : CustomersIterable) {
                Customers.add(Customer);
            }
            Iterable<productMaster> Products = productMasterRepository.findBySplCustCode(Customers.get(0).getCode().toString());
            int size = 0;
            for (productMaster value : Products) {
                size++;
            }
            if (size == 0) {
                Products = productMasterRepository.findBySplCustCode("");
            }
            // List<dailySales> orders = dailySalesRepository.findByOrderNo(orderNo);
            model.addAttribute("products", Products);
            model.addAttribute("orders", dailySalesRepository.findByOrderNo(orderNo));
            model.addAttribute("sales", saleOrderRepository.findByOrderNo(orderNo));
            return "view_order";
        }
        List messages = new ArrayList<>();
        messages.add("Login First");
        model.addAttribute("messages", messages);
        return "redirect:/login";
    }
    @PostMapping(value = "/updateOrder")
    public ModelAndView updateOrder(Model model, @RequestBody List<dailySales> orderList, HttpSession session) {

        if (session.getAttribute("loggedIn").equals("yes")) {
            System.out.println("log info -- dailySalesList " + orderList.size());



            for (dailySales list : orderList) {
                System.out.println(list.getQuantity() + " --- " + list.getDisc() + " --- " + list.getAmount() + " --- " + list.getNetAmount() + " --- " + list.getId()+"---"+list.getTotDisc() +"---"+list.getTotAmount()+"---"+list.getTotNetAmount());
                dailySales ds = dailySalesRepository.findByid(list.getId());
                //dailySales ds = new dailySales();
                System.out.println("hello");
                ds.setQuantity(list.getQuantity());
                ds.setDisc(list.getDisc());
                ds.setAmount(list.getAmount());
                ds.setNetAmount(list.getNetAmount());
                ds.setTotDisc(list.getTotDisc());
                ds.setTotAmount(list.getTotAmount());
                ds.setTotNetAmount(list.getTotNetAmount());
                ds.setDate(list.getDate());

                dailySalesRepository.save(ds);
            }

            saleOrder so = saleOrderRepository.findByOrderNo(orderList.get(0).getOrderNo());
            so.setDate(orderList.get(0).getDate());
            so.setCustCode(orderList.get(0).getCustCode());
            so.setDisc(orderList.get(0).getTotDisc());
            so.setAmount(orderList.get(0).getTotAmount());
            so.setNetAmount(orderList.get(0).getTotNetAmount());

            saleOrderRepository.save(so);

            //return "redirect:/order/view/63";
            //return new ModelAndView("/order/view/63");
            return new ModelAndView("/login");

        }
        List messages = new ArrayList<>();
        messages.add("Login First");
        model.addAttribute("messages", messages);
        //return "redirect:/login";
        return new ModelAndView("/login");
    }


    @PostMapping(value = "/createOrderNew")
    public ModelAndView createOrderNew(Model model, @RequestBody List<dailySales> orderList, HttpSession session) {

        if (session.getAttribute("loggedIn").equals("yes")) {
            System.out.println("log info -- dailySalesList " + orderList.size());
            Iterable<customer> CustomersIterable = customerRepository.findAll();
            List<customer> Customers = new ArrayList<>();
            for (customer Customer : CustomersIterable) {
                Customers.add(Customer);
            }
            model.addAttribute("customers", Customers);


            Double totalAmount=0.0, totalNetAmount=0.0;
            for (dailySales list : orderList) {
                dailySales ds = new dailySales();

                ds.setAmount(list.getAmount());
                ds.setNetAmount(list.getNetAmount());



                totalAmount =totalAmount + list.getAmount();
                totalNetAmount =totalNetAmount + list.getNetAmount();
            }

            for (dailySales list : orderList) {
                dailySales ds = new dailySales();
                ds.setProdCode(list.getProdCode());
                ds.setCustCode(list.getCustCode());
                ds.setUnitRate(list.getUnitRate());
                ds.setQuantity(list.getQuantity());
                ds.setDisc(list.getDisc());
                ds.setAmount(list.getAmount());
                ds.setNetAmount(list.getNetAmount());
                ds.setOrderNo(list.getOrderNo());
                ds.setTotAmount(totalAmount);
                ds.setTotNetAmount(totalNetAmount);
                ds.setTotDisc(totalAmount-totalNetAmount);
                ds.setDate(list.getDate());
                System.out.println(list.getDate());

                dailySalesRepository.save(ds);
            }

            System.out.println("log info -- dailySalesList " +totalAmount);
            System.out.println("log info -- dailySalesList " + totalNetAmount);

            saleOrder so = new saleOrder();
            so.setCustCode(orderList.get(0).getCustCode());
            so.setOrderNo(orderList.get(0).getOrderNo());
            so.setDisc(totalAmount-totalNetAmount);
            so.setAmount(totalAmount);
            so.setNetAmount(totalNetAmount);
           so.setDate(orderList.get(0).getDate());

            saleOrderRepository.save(so);

            //return "redirect:/order/view/63";
            //return new ModelAndView("/order/view/63");
            return new ModelAndView("/login");

        }
        List messages = new ArrayList<>();
        messages.add("Login First");
        model.addAttribute("messages", messages);
        //return "redirect:/login";
        return new ModelAndView("/login");
    }




    @GetMapping("/orders/{id}")
    public String deleteOrder(@PathVariable Integer id,Model model,HttpSession session) {
        if (session.getAttribute("loggedIn").equals("yes")) {
            dailySalesRepository.deleteById(id);
            return "redirect:/";
        }
        List messages = new ArrayList<>();
        messages.add("Login First");
        model.addAttribute("messages", messages);
        return "redirect:/login";
    }

}



