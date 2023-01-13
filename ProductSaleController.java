package com.logi.pro.controller;






import java.awt.print.PrinterException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.print.PrintException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import com.logi.pro.dao.AndroidUserDao;
import com.logi.pro.dao.CustomerDao;
import com.logi.pro.dao.LedgerHeadDao;
import com.logi.pro.dao.ProductRequestDao;
import com.logi.pro.dao.ProductSaleDao;
import com.logi.pro.dao.ProductSaleReturnDao;
import com.logi.pro.dao.QuotationDao;
import com.logi.pro.dao.SaleClosingDao;
import com.logi.pro.dao.StoreDao;
import com.logi.pro.domain.AndroidProductRequest;
import com.logi.pro.domain.AndroidUser;
import com.logi.pro.dao.TripMasterDao;
import com.logi.pro.domain.Customer;
import com.logi.pro.domain.DeliveryOrder;
import com.logi.pro.domain.JobCard;
import com.logi.pro.domain.Ledger;
import com.logi.pro.domain.LedgerHead;
import com.logi.pro.domain.Pagination;
import com.logi.pro.domain.Product;
import com.logi.pro.domain.ProductSale;
import com.logi.pro.domain.ProductSaleLineItem;
import com.logi.pro.domain.ProductSaleReturnLineItem;
import com.logi.pro.domain.Purchase;
import com.logi.pro.domain.Quotation;
import com.logi.pro.domain.RentelDetailsLineitem;
import com.logi.pro.domain.SaleClosing;
import com.logi.pro.domain.Store;
import com.logi.pro.domain.StoreProduct;
import com.logi.pro.domain.Supplier;
import com.logi.pro.domain.TripMaster;
import com.logi.pro.domain.UnitOfMeasurement;
import com.logi.pro.services.LedgerHeadService;
import com.logi.pro.services.LedgerService;
import com.logi.pro.services.ProductSalePrintService;
import com.logi.pro.services.ProductSaleService;
import com.logi.pro.services.ProductService;
import com.logi.pro.services.RentelService;
import com.logi.pro.services.StoreService;
import com.logi.pro.services.TaxRatesService;
import com.logi.pro.util.CalendarUtil;
import com.logi.pro.util.Constants;
import com.logi.pro.validator.ProductSaleValidator;
import com.logi.pro.services.ProductRequestService;
import com.logi.pro.controller.SessionController;

@Controller

public class ProductSaleController extends GenericController{

	protected final Log logger = LogFactory.getLog(getClass());

	
	@Autowired
	private TripMasterDao trpDao;
	@Autowired
	private StoreDao stDao;
	@Autowired
	private ProductSaleDao psDao;
	@Autowired
	private ProductRequestService aprqService ;
	
	@Autowired
	private ProductSaleService psService ;
	@Autowired
	private ProductSalePrintService psprintService ;
	
	@Autowired
	private StoreService stService ;
	
	@Autowired
	private ProductSaleValidator psvalidator ;
	
	@Autowired
	private ProductSaleReturnDao psrDao ;
	@Autowired
	private LedgerHeadService lehService ;
	
	@Autowired
	private CustomerDao cuDao ;
	@Autowired
	private ProductService prService ;
	
	@Autowired
	private LedgerService leService ;
	
	@Autowired
	private LedgerHeadDao lehDao ;
	
	@Autowired
	private TaxRatesService taxService ;
	@Autowired
	private SaleClosingDao sclDao;
	
	@Autowired
	private RentelService rentService;
	
	@Autowired
	private QuotationDao qtDao;
	
	@Autowired
	private ProductRequestDao aprqDao;
	
	@Autowired
	private AndroidUserDao auserDao;
	
	@RequestMapping(value="/ps/counter", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView ps_counter(ProductSale ps,HttpServletRequest request) {
		
		logger.info("***Product Sale add  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");

	     //setListBoxValues(mv, request);
		ps.setPsdate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
		ps.setDuedate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
		
		
		
		
		
/*		ps.setPsdate(CalendarUtil.getDateTimeToday(Constants.DATE_FIRST));
*/		//set store details to sttr
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		String saletype=(String)request.getSession().getAttribute("sessionstoretype");
		ps.setSaletype(saletype);
	
		ps.setStdename("PRODUCT SALE");  
		ps.setCuname("COUNTER"); 
		ps.setLineitem(new ProductSaleLineItem());
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		
		ps.setPhnno("0");  
		setEnteredByValues(request, ps);
				
		//
		ps.setPaymenttype(Constants.PAYMENTTYPE_CASH);
		ps.setLaction("insert");
		
		Customer cu=cuDao.getCuRecord1("COUNTER");
		
		if(cu!=null) {
			
		ps.setCuname(cu.getCuname());
		ps.setCunid(cu.getCunid());
		ps.setDuedt(cu.getDuedt());
		}
		ps.setAutoadd("YES");
		ps.setMyaction("insert");
		 if(sessionsaletype !=null && sessionsaletype.contentEquals("inpos")) {
				
				mv = new ModelAndView("ps_add_cou_smpl");
		
	 }
	
		
		else if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				ProductSale ps1=psDao.getLastTripData(ps.getStname());
			if(ps1 !=null) {
					ps.setTrpname(ps1.getTrpname());
					
				
				
					ps.setTickettype(ps1.getTickettype());
					if(ps1.getTickettype() !=null &&ps1.getTickettype().equals("parcel")) {
						ps.setCartons("1");
					}
					if(ps1.getTolocation() !=null ) {
						ps.setTolocation(ps1.getTolocation());
						ps.setToshortname(ps1.getToshortname());
						}
					
					if(ps1.getFrmlocation() !=null ) {
						ps.setFrmlocation(ps1.getFrmlocation());
						ps.setFrmstortname(ps1.getFrmstortname());
						}
				if( ps1.getSmancode() !=null) {
					ps.setSmancode(ps1.getSmancode());
					}
					
					
					
			}

			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		
				}
		else {
			mv = new ModelAndView("ps_add_cou");
		}
		mv.addObject("cophsrequests", aprqService.getPsCoureqRecords((String)request.getSession().getAttribute("sessionUtype"),(String)request.getSession().getAttribute("sessionUStname")));
		String printstatus=request.getParameter("printstatus");
		String printid=request.getParameter("printid");

		if (printstatus!=null) {
			mv.addObject("printstatus", printstatus);

		}
		if (printid!=null) {
			mv.addObject("printid", printid);

		}
		

		 mv.addObject("ps",ps);
		
		mv.addObject("uomlist", prService.getAllUomList() );
		
		return mv;
	}
	
	
	@RequestMapping(value="/ps/resale", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView resale(ProductSale ps,HttpServletRequest request,BindingResult result) {
		
		logger.info("***Product Sale resale  add  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		else if(sessionsaletype.equals("trnspot")){
		mv = new ModelAndView("ps_add_cou_transport");
		}
		else {
			mv = new ModelAndView("ps_add_cou");
		}
	     //setListBoxValues(mv, request);
		ps.setPsvno(request.getParameter("psvno"));
		ps.setAction("resale");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
	ps=	psService.resalePs(request.getParameter("psnid"));
	
	mv = new ModelAndView("redirect:/ps/edit?psnid="+ps.getPsnid());	
		}
		else{
			
			
			mv = new ModelAndView("ps_list");
			
				if(sessionsaletype.equals("trnspot")) {
					 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
			        	
			        }
			mv.addObject("ps",ps);
			
	
	}
	
		return mv;
	}
	
	@RequestMapping(value = "ps/counter/s_pt", method = RequestMethod.POST)
	public ModelAndView ps_counter_select(@ModelAttribute(value = "ps") ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		logger.info("*** select_paymentype  ***:pt"+ps.getPaymenttype());
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		mv = new ModelAndView("ps_add_cou");
		
		
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		mv.addObject("uomlist", prService.getAllUomList() );
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
			if(sessionsaletype.equals("trnspot")) {
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
		return mv;
	}
	   
	@RequestMapping(value = "ps/select/cu", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_select_cu(@ModelAttribute(value = "ps") ProductSale ps,
			 HttpServletRequest request,BindingResult result) {
		logger.info("*** select_cu  ***:pt");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		String cuname=request.getParameter("cuname");
		mv = new ModelAndView("ps_add_cou");
		
		Customer cu = psService.getCuRecord1(cuname);
		ps.setCuname(cu.getCuname());
		ps.setCunid(cu.getCunid());
		ps.setDuedt(cu.getDuedt());
		ps.setPhnno(cu.getCubmob());
		String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		else if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			ps.setConsignee(ps.getCuname());
			ps.setConsignertelno(ps.getPhnno());
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}
		//BeanUtils.copyProperties(curService.getCurrateTodaysRecord(p.getCurname()), p);
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		mv.addObject("uomlist", prService.getAllUomList() );
	
	//ledger balance
		LedgerHead leh =lehDao.getLehRecordByName(ps.getCuname());
		mv.addObject("opbal",leService.getOpeingBalaneOfCu(leh.getLehnid()));
		 
		mv.addObject("ps",ps);
		return mv;
	}
	
	
	@RequestMapping(value = "ps/otherDiscountUpdate", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView PsotherDiscountUpdate(@ModelAttribute(value = "ps") ProductSale ps,
			 HttpServletRequest request,BindingResult result) {
		logger.info("*** select_cu  ***:pt");
		

		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		else if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			ps.setConsignee(ps.getCuname());
			ps.setConsignertelno(ps.getPhnno());
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}
		
		psService.otherDiscountUpdate(ps);
		if(!request.getParameter("type").equals("hns")) {
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		
		mv.addObject("ps",psService.getPsRecord(ps.getPsnid()));
		}
		else {
			return new ModelAndView("redirect:/ps/counter");
		}
		
		
		return mv;
	}
	
	@RequestMapping(value = "ps/othersubDiscountUpdate", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView PsothersubDiscountUpdate(@ModelAttribute(value = "ps") ProductSale ps,
			 HttpServletRequest request,BindingResult result) {
		logger.info("*** select_cu  ***:pt");
		ModelAndView mv = new ModelAndView("ps_add_cou");
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		else if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
		mv = new ModelAndView("ps_add_cou");
		}
		psService.otherSubDiscountUpdate(ps);
		if(!request.getParameter("type").equals("hns")) {
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		
		mv.addObject("ps",psService.getPsRecord(ps.getPsnid()));
		}
		else {
			return new ModelAndView("redirect:/ps/counter");
		}
		
		
		return mv;
	}
	
	@RequestMapping(value = "ps/select/unit", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_select_unit(@ModelAttribute(value = "ps") ProductSale ps,
			 HttpServletRequest request,BindingResult result) {
		logger.info("*** select_cu  ***:pt");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		String prunit=request.getParameter("prunit");
		String prnid=request.getParameter("prnid");
	
		String prdefaultuomfactor=request.getParameter("prdefaultuomfactor");
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		else	if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
		else {
		mv = new ModelAndView("ps_add_cou");
		}
		
		
		UnitOfMeasurement saleuomfactor = prService.getUomRecordByPrBUnitAndPr(prunit,prnid);
		
		if(saleuomfactor.getUomprsrate()!=null&&saleuomfactor.getUomprsrate()>0.00) {
			
			
			BigDecimal prsrate = new BigDecimal("0.0000");
			
			 prsrate= new BigDecimal(saleuomfactor.getUomprsrate());
		
			 //double prprate=Double.parseDouble(ps.getLineitem().getPrdefaultprprate())/ (Double.parseDouble(prdefaultuomfactor)/Double.parseDouble(saleuomfactor));
			
			if(ps.getLineitem().getPslqty1()==null) {
				ps.getLineitem().setPslqty1(0.00);
			}
			
			ps.getLineitem().setPslqty(ps.getLineitem().getPslqty1()*(saleuomfactor.getUomfactor()));
			ps.getLineitem().setUomfactor(""+saleuomfactor.getUomfactor());
			ps.getLineitem().setPrsrate(prsrate);
		//	ps.getLineitem().setPrprate(new BigDecimal(prprate).setScale(3, RoundingMode.UP));
			
			ps.getLineitem().setPsltotal2((ps.getLineitem().getPrsrate().multiply(new BigDecimal(ps.getLineitem().getPslqty1()))).setScale(3, RoundingMode.HALF_UP));
			//BeanUtils.copyProperties(curService.getCurrateTodaysRecord(p.getCurname()), p);
			ps.getLineitem().setPsldisper(new BigDecimal(0.000));
			ps.getLineitem().setPsldisprice2(new BigDecimal(0.000));
			
			
			ps.getLineitem().setTaxrates(ps.getLineitem().getTaxrates());
	        Double tottax=(ps.getLineitem().getTaxrates()/100)*ps.getLineitem().getPrsrate().doubleValue();
				BigDecimal bd = new BigDecimal(tottax);
			    bd = bd.setScale(3, RoundingMode.FLOOR);
			    ps.getLineitem().setTaxamounttot(bd.doubleValue());
			    ps.getLineitem().setTaxamount((ps.getLineitem().getTaxrates()/100)*ps.getLineitem().getPrsrate().doubleValue());

			    ps.getLineitem().setPsltotal(ps.getLineitem().getPsltotal2().setScale(3, RoundingMode.UP));
			    ps.getLineitem().setPsltotal(ps.getLineitem().getPsltotal().add(new BigDecimal(ps.getLineitem().getTaxamounttot())));

			    
			
		}
		else {
			double prsrate = 0.000;
			
			 prsrate=(Double.parseDouble(ps.getLineitem().getPrdefaultprsrate())/Double.parseDouble(prdefaultuomfactor))*saleuomfactor.getUomfactor();
		
			 BigDecimal prsratebig = new BigDecimal(prsrate);
			 MathContext m = new MathContext(5);
			 BigDecimal b2 = prsratebig.round(m);
			
			if(ps.getLineitem().getPslqty1()==null) {
				ps.getLineitem().setPslqty1(0.00);
			}
			
			ps.getLineitem().setPslqty(ps.getLineitem().getPslqty1()*(saleuomfactor.getUomfactor()));
			ps.getLineitem().setUomfactor(""+saleuomfactor.getUomfactor());
			ps.getLineitem().setPrsrate(b2);
			System.out.println("second"+ps.getLineitem().getPrsrate());

		//	ps.getLineitem().setPrprate(new BigDecimal(prprate).setScale(3, RoundingMode.UP));
			
			ps.getLineitem().setPsltotal2((ps.getLineitem().getPrsrate().multiply(new BigDecimal(ps.getLineitem().getPslqty1()))).setScale(3, RoundingMode.HALF_UP));
			//BeanUtils.copyProperties(curService.getCurrateTodaysRecord(p.getCurname()), p);
			ps.getLineitem().setPsldisper(new BigDecimal(0.000));
			ps.getLineitem().setPsldisprice2(new BigDecimal(0.000));
			
			ps.getLineitem().setTaxrates(ps.getLineitem().getTaxrates());
	        Double tottax=(ps.getLineitem().getTaxrates()/100)*ps.getLineitem().getPrsrate().doubleValue();
				BigDecimal bd = new BigDecimal(tottax);
			    bd = bd.setScale(3, RoundingMode.FLOOR);
			    ps.getLineitem().setTaxamounttot(bd.doubleValue());
			    ps.getLineitem().setTaxamount((ps.getLineitem().getTaxrates()/100)*ps.getLineitem().getPrsrate().doubleValue());

			    ps.getLineitem().setPsltotal(ps.getLineitem().getPsltotal2().setScale(3, RoundingMode.UP));
			    ps.getLineitem().setPsltotal(ps.getLineitem().getPsltotal().add(new BigDecimal(ps.getLineitem().getTaxamounttot())));

			    
			
		}
		
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		mv.addObject("uomvallist", prService.getUomListByPrbAndPr(ps.getLineitem().getBunit(),ps.getLineitem().getPrdefaultunit(),ps.getLineitem().getPrnid()) );
		mv.addObject("taxlist", taxService.getTaxRateList() );
		mv.addObject("ps",ps);
		return mv;
	}
	@RequestMapping(value = "ps/select/leh", method = RequestMethod.POST)
	public ModelAndView ps_select_leh(@ModelAttribute(value = "ps") ProductSale ps,
			 HttpServletRequest request,BindingResult result) {
		logger.info("*** select_le  ***:head"+ps.getTowardssource());
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		String lehead=request.getParameter("towardssource");
		mv = new ModelAndView("ps_add_cou");
		ps.setAction("select head");
		//  leValidator.validate(le, result);
	//	if (!result.hasErrors()) {
		LedgerHead leh = lehService.getLeRecord1(lehead);
		
		ps.setTowardssource(leh.getLehname());
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps", ps);
	//	}

		return mv;
	}
	
	@RequestMapping(value="/ps/cou/add", method = RequestMethod.GET)
	public ModelAndView ps_cou_add(ProductSale ps,HttpServletRequest request) {
		
		logger.info("***Product Sale counter add  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
	String sessionsaletype=(String)request.getSession().getAttribute("sessionSaletype");
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		else if(sessionsaletype.equals("trnspot")){
			mv = new ModelAndView("ps_add_cou_transport");
			}
		else {
		mv = new ModelAndView("ps_add_cou");
		}
		//setListBoxValues(mv, request);
		
		ps.setPsdate(CalendarUtil.getDateTimeToday(Constants.DATE_FIRST));
		//set store details to sttr
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.setStdename((String)request.getSession().getAttribute("sessionUStdename"));
		setEnteredByValues(request, ps);
				
		//
		ps.setPaymenttype(Constants.PAYMENTTYPE_CASH);
		//set ware house
		Store st = stService.getStRecord(ps.getStnid());
		ps.setWnid(st.getWnid());
		ps.setWname(st.getWname());
	
		//copy patv to ps
		
		ps.setLaction("insert");
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		return mv;
	}
	
	@RequestMapping(value="ps/edit", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView ps_edit(ProductSale ps,HttpServletRequest request) {
	
		logger.info("***ps edit  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		String psnid =request.getParameter("psnid");
	
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			else if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}
		
		//setListBoxValues(mv, request);
		
		ps = psService.getPsRecord(psnid);
		setEnteredByValues(request, ps);
		ps.setLaction("insert");
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.setStdename("PRODUCT SALE");  
		String saletype=(String)request.getSession().getAttribute("sessionstoretype");
		ps.setSaletype(saletype);
		ps.setLineitem(new ProductSaleLineItem());
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setMyaction("insert");
		mv.addObject("ps",ps);
		mv.addObject("pslineitems",psService.getPslRecords(psnid));
		mv.addObject("uomlist", prService.getAllUomList() );
		//setListBoxValues(mv, request);
		return mv;
	}

	
	@RequestMapping(value = "ps/select/prcode", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_select_prcode(@ModelAttribute(value = "ps") ProductSale ps,
		 HttpServletRequest request,BindingResult result) {
		
		logger.info("*** select product by pr code***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}

		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}
		
			
		ps.setFrmlocation(request.getParameter("frmlocation"));
		ps.setTolocation(request.getParameter("tolocation"));
	ps.setSsionsaletype((String) request.getSession().getAttribute("sessionSaletype"));
		ps.setStname(request.getParameter("stname"));	
		ps.setStdename(request.getParameter("stdename"));
		
		ps.setCuname(request.getParameter("cuname"));
		ps.setPaymenttype(request.getParameter("paymenttype"));
		ps.setPsdate(request.getParameter("psdate"));
		ps.setPslnettotal2(new BigDecimal(request.getParameter("pslnettotal2")));
		ps.setPsltotdisprice2(new BigDecimal(request.getParameter("psltotdisprice2")));
		ps.setPslnettotal(new BigDecimal(request.getParameter("pslnettotal")));
		ps.setPslnettotal(new BigDecimal(request.getParameter("pslnettotal")));
		ps.setCrdlnettotal(new BigDecimal(request.getParameter("crdlnettotal")));
		ps.setReceived(Double.valueOf(request.getParameter("received")));
		ps.setBalance(Double.valueOf(request.getParameter("balance")));
		ps.setEnteredby((String)request.getSession().getAttribute("sessionUname"));
		
		
	
		ProductSaleLineItem psl = new ProductSaleLineItem();
		psl.setPrcode(request.getParameter("prcode"));
		psl.setPrbarcode(request.getParameter("prbarcode"));
		psl.setPrname(request.getParameter("prname"));
		psl.setStname(request.getParameter("stname"));
		psl.setStdename(request.getParameter("stdename"));
		psl.setPrnid(request.getParameter("prnid"));

		ps.setLineitem(psl);
		ps.setAction("select_prcode");
		ps.setLaction("insert");
		ps.setSessionretailrate((String)request.getSession().getAttribute("sessiondefaultprs"));
	    ps.setSessiStringwholerate((String)request.getSession().getAttribute("sessiondefaultprswhole"));
		 
	    	
//		System.out.println(ps.getSaletype()+"defhje"+	ps.getSessionretailrate()+ps.getSessiStringwholerate());
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
			ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
			String stname ="";
			String utype = (String)request.getSession().getAttribute("sessionUtype");
			
			if(utype.equals("Superuser")) {
				stname="all";
			}
			else {
				 stname = ((String)request.getSession().getAttribute("sessionUStname"));
			}
			String defaultprs;
			if(ps.getSaletype().equals("wh")) {
				
				defaultprs="sessiondefaultprswhole";
			}
			else {
			
				defaultprs="sessiondefaultprs";
			}
	
			psService.getProduct(ps,(String)request.getSession().getAttribute(defaultprs),(String)request.getSession().getAttribute("sessionprsdefaultunit"),(String)request.getSession().getAttribute("sessionprcodebar"),stname,(String)request.getSession().getAttribute("sessionPsline"),request.getParameter("bartype"),request.getParameter("prsrate"));
				
			
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		else{
			  
			ps.setLineitem(new ProductSaleLineItem());
			
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
			
		}
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		////setListBoxValues(mv, request);
		return mv;
	}
	

	@RequestMapping(value = "ps/select/prcodeorbar", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_select_prcodeorbar(@ModelAttribute(value = "ps") ProductSale ps,
		 HttpServletRequest request,BindingResult result) {
		
		logger.info("*** select product by pr code***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}	
		
	
		ps.setStname(request.getParameter("stname"));	
		ps.setStdename(request.getParameter("stdename"));
		
		ps.setCuname(request.getParameter("cuname"));
		ps.setPaymenttype(request.getParameter("paymenttype"));
		ps.setPsdate(request.getParameter("psdate"));
		ps.setPslnettotal2(new BigDecimal(request.getParameter("pslnettotal2")));
		ps.setPsltotdisprice2(new BigDecimal(request.getParameter("psltotdisprice2")));
		ps.setPslnettotal(new BigDecimal(request.getParameter("pslnettotal")));
		ps.setPslnettotal(new BigDecimal(request.getParameter("pslnettotal")));
		ps.setCrdlnettotal(new BigDecimal(request.getParameter("crdlnettotal")));
		ps.setReceived(Double.valueOf(request.getParameter("received")));
		ps.setBalance(Double.valueOf(request.getParameter("balance")));

		ps.setEnteredby((String)request.getSession().getAttribute("sessionUname"));
		
	
		ProductSaleLineItem psl = new ProductSaleLineItem();
		psl.setPrcode(request.getParameter("prcode"));
		psl.setPrbarcode(request.getParameter("prbarcode"));
		psl.setPrname(request.getParameter("prname"));
		psl.setStname(request.getParameter("stname"));
		psl.setStdename(request.getParameter("stdename"));
		psl.setPrnid(request.getParameter("prnid"));

		ps.setLineitem(psl);
		ps.setAction("select_prcode");
		ps.setLaction("insert");
		ps.setSessionretailrate((String)request.getSession().getAttribute("sessiondefaultprs"));
	    ps.setSessiStringwholerate((String)request.getSession().getAttribute("sessiondefaultprswhole"));
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
			ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
			String stname ="";
			String utype = (String)request.getSession().getAttribute("sessionUtype");
			
			if(utype.equals("Superuser")) {
				stname="all";
			}
			else {
				 stname = ((String)request.getSession().getAttribute("sessionUStname"));
			}
			String defaultprs;
			if(ps.getSaletype().equals("wh")) {
				defaultprs="sessiondefaultprswhole";
			}
			else {
				defaultprs="sessiondefaultprs";
			}
		
		    	
			
			psService.getProduct(ps,(String)request.getSession().getAttribute(defaultprs),(String)request.getSession().getAttribute("sessionprsdefaultunit"),(String)request.getSession().getAttribute("sessionprcodebar"),stname,(String)request.getSession().getAttribute("sessionPsline"),request.getParameter("bartype"),request.getParameter("prsrate"));
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		else{
			  
			ps.setLineitem(new ProductSaleLineItem());
			
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
			
		}
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		////setListBoxValues(mv, request);
		return mv;
	}
	
	
	@RequestMapping(value = "ps/insertl", method = RequestMethod.POST)
	public ModelAndView psl_insert(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** PS lineItem insert ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("ps_add_cou");
			
		ps.setAction("insert_lineitem");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
			psService.batchInsertPsl(new Product(),ps, new StoreProduct(),(String)request.getSession().getAttribute("sessionprsdefaultunit"),(String)request.getSession().getAttribute("sessionPsline"));
	
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
			//reset psl
			ps.setLineitem(new ProductSaleLineItem());
			ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
			ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		
		}
		else{
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}

		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList(ps.getStname()));
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		////setListBoxValues(mv, request);
		return mv;
	}
	
	@RequestMapping(value = "ps/pslupdateesy", method = RequestMethod.POST)
	public ModelAndView pslupdateesy(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** PS pslupdateesy  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("ps_add_cou");
			
		ps.setAction("insert_lineitem");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
		//	psService.pslUpdateEasy(ps,request.getParameter("type"));
	
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
			//reset psl
			ps.setLineitem(new ProductSaleLineItem());
			ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
			ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		
		}
		else{
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList(ps.getStname()));
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		////setListBoxValues(mv, request);
		return mv;
	}
	
	@RequestMapping(value = "ps/insertl_1", method = RequestMethod.POST)
	public ModelAndView psl_insert1(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** PS lineItem insert ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("ps_add_cou");
			
		ps.setAction("insert_lineitem");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
			psService.batchInsertPsl1(ps,(String)request.getSession().getAttribute("sessionprsdefaultunit"),(String)request.getSession().getAttribute("sessionPsline"));
	
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
			//reset psl
			ps.setLineitem(new ProductSaleLineItem());
		}
		else{
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		ps.setAction("edit_update");
		ps.setLaction("edit_update_insert");
		
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
	    ps.setMyaction("edit");
	    String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}

	    mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		 
		////setListBoxValues(mv, request);
		return mv;
	}
	@RequestMapping(value = "ps/save", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_save(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** save product sales  ***:");
		
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps/counter");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
        mv = new ModelAndView("ps_list");
        ps.setAction("insert");
        String editpsdate= (String)request.getSession().getAttribute("sessionEditPsdate");
        String cashamntreccive=(String) request.getSession().getAttribute("sesionCashamntreccive");
        ps.setCashamntreccive(cashamntreccive);
        String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
        if(sessionsaletype.equals("trnspot")) {
        	psService.setTripDetails(ps);
        	
        }
        
        if(editpsdate !=null){
        	ps.setEditpsdate(editpsdate);
        }
        else{
        	ps.setEditpsdate("");
        }
		ps.setUtype((String)request.getSession().getAttribute("sessionUtype"));

        psvalidator.validate(ps, result);
	 
		if (!result.hasErrors()) {
			psService.batchInsert(ps);
			mv = new ModelAndView("redirect:/ps/counter");	
			
		}else{
			

			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}
	
				mv.addObject("uomlist", prService.getAllUomList() );
				
				
					if(sessionsaletype.equals("trnspot")) {
						 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
				        	
				        }
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		//setListBoxValues(mv, request);
		return mv;
	}
	@RequestMapping(value = "ps/saveprint", method = RequestMethod.POST)
	public ModelAndView saveprint(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) throws DocumentException, IOException, PrinterException {
		
		logger.info("*** save product sales  ***:");
		
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps/counter");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
        mv = new ModelAndView("ps_list");
        ps.setAction("insert");
        String editpsdate= (String)request.getSession().getAttribute("sessionEditPsdate");
        String cashamntreccive=(String) request.getSession().getAttribute("sesionCashamntreccive");
        ps.setCashamntreccive(cashamntreccive);
        String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
        if(sessionsaletype.equals("trnspot")) {
        	psService.setTripDetails(ps);
        	
        }
        
        if(editpsdate !=null){
        	ps.setEditpsdate(editpsdate);
        }
        else{
        	ps.setEditpsdate("");
        }
		ps.setUtype((String)request.getSession().getAttribute("sessionUtype"));

        psvalidator.validate(ps, result);
	 
		if (!result.hasErrors()) {
			psService.batchInsert(ps);

			String printtype=(String)request.getSession().getAttribute("sessionprintertype");
			if(printtype.equals("direct")){
				mv = new ModelAndView("redirect:/ps/printtdir?psnid="+ps.getPsnid());	

			}
			else{
				if(ps.getTickettype() !=null && ps.getTickettype().equals("parcel")) {
					mv = new ModelAndView("redirect:/ps/list?printstatus="+"print"+"&printid="+ps.getPsnid()+"&psvno="+ps.getPsvno()+"&psdate="+ps.getPsdate());
				}
				else {
					mv = new ModelAndView("redirect:/ps/counter?printstatus="+"print"+"&printid="+ps.getPsnid());
				}
					

			}
			
		}else{

			
			
			if(sessionsaletype.equals("inpos")){
				mv = new ModelAndView("ps_add_cou_smpl");
			}
			
			if(sessionsaletype.equals("trnspot")) {
				mv = new ModelAndView("ps_add_cou_transport");
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
			else {
				mv = new ModelAndView("ps_add_cou");
				}
	
				mv.addObject("uomlist", prService.getAllUomList() );
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
			 if(sessionsaletype.equals("trnspot")) {
				 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
		        	
		        }
		}
		//setListBoxValues(mv, request);
		return mv;
	}
	
	@RequestMapping(value = "ps/update", method = RequestMethod.POST)
	public ModelAndView ps_update(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** update product sales  ***:"+ps.getPsnid());
		
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps/counter");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
        mv = new ModelAndView("ps_list");
        ps.setAction("update");
       ps.setLaction("edit_update_insert");
        psvalidator.validate(ps, result);
	 
		if (!result.hasErrors()) {
			psService.batchUpdate(ps);
			mv = new ModelAndView("redirect:/ps/counter");	
			
		}else{
			ps.setAction("edit_update");
			ps.setLaction("edit_update_insert");
			mv.addObject("ps",ps);
			mv = new ModelAndView("ps_add_cou");
			mv.addObject("uomlist", prService.getAllUomList() );
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}

		//setListBoxValues(mv, request);
		return mv;
	}
	
	@RequestMapping(value="ps/edit_1", method = {RequestMethod.GET})
	public ModelAndView ps_edit1(@ModelAttribute(value = "ps")  ProductSale ps,HttpServletRequest request) {
	
		logger.info("***ps edit  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
	
		String psnid =request.getParameter("psnid");
	
		String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}

			
		//setListBoxValues(mv, request);
		
		ps = psService.getPsRecord(psnid);
		ps.setAction("edit_update");
		ps.setLaction("edit_update_lineitem");
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.setStdename("PRODUCT SALE");  
		ps.setLineitem(new ProductSaleLineItem());
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
	    ps.setMyaction("edit");
		mv.addObject("ps",ps);
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("pslineitems",psService.getPslRecords(psnid));
		
		
		return mv;
	}

	@RequestMapping(value = "ps/de", method = RequestMethod.POST)
	public ModelAndView ps_de(@ModelAttribute(value = "de")  DeliveryOrder de,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** product sles from delivery  ***:");
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		setEnteredByValuesForDE(request, de);
		/*
		ProductSale ps = new ProductSale();
		ps.setDenid(de.getDenid());
		ps.setAction("delivery");
        psvalidator.validate(ps, result); */
        
        mv = new ModelAndView("ps_list");
        
        psService.batchInsertDe(de);
        mv = new ModelAndView("redirect:/ps/counter");	
        return mv;
	}
	
	@RequestMapping(value="/ps/editl", method = RequestMethod.POST)
	public ModelAndView psl_edit(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request) {
		logger.info("*** supplier edit ***:");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("ps_add_cou");
			
		String pslnid = request.getParameter("pslnid");
		ps.setLineitem(psService.getPslRecord(pslnid));
	
		String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}
		ps.setAction("edit_lineitem");
		ps.setLaction("update_lineitem");
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		ps.setMyaction("updateitem");
		mv.addObject("ps",ps);
		mv.addObject("uomvallist", prService.getUomListByPrbAndPr(ps.getLineitem().getBunit(),ps.getLineitem().getPrdefaultunit(),ps.getLineitem().getPrnid()) );
		mv.addObject("taxlist", taxService.getTaxRateList() );
		return mv;
	}
	@RequestMapping(value="/ps/editl_1", method = RequestMethod.POST)
	public ModelAndView psl_edit1(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request) {
		logger.info("*** supplier edit ***:");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("ps_add_cou");
			
		String pslnid = request.getParameter("pslnid");
		ps.setLineitem(psService.getPslRecord(pslnid));
	
		ps.setAction("edit_update");
		
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		ps.setLaction("edit_update_lineitem");
		mv.addObject("ps",ps);
		mv.addObject("uomlist", prService.getAllUomList() );
		ps.setMyaction("editupdateitem");
		return mv;
	}
	
	@RequestMapping(value = "ps/updatel", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView psl_update(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** ps lineItem update ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		 
		 
		String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}

			if(ps.getLineitem()!=null) {
			BeanUtils.copyProperties(ps,ps.getLineitem() );
			
		ps.setAction("update_lineitem");
		ps.setLaction("update_lineitem");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
		
			psService.updatePsl(ps.getLineitem());
			ps.setLineitem(new ProductSaleLineItem());
			ps.setLaction("insert");//reset line item action
			ps.getLineitem().setStname(ps.getStname());
			ps.getLineitem().setStnid(ps.getStnid());
		}else {
			mv.addObject("uomvallist", prService.getUomListByPrbAndPr(ps.getLineitem().getBunit(),ps.getLineitem().getPrdefaultunit(),ps.getLineitem().getPrnid()) );
			mv.addObject("taxlist", taxService.getTaxRateList() );
		}
		
			}
		
			
			
			
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.setStdename("PRODUCT SALE");  
	
		ps.setMyaction("insert");
	
		
	//	mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		//setListBoxValues(mv, request);
		return mv;
	}
	@RequestMapping(value = "ps/updatel_1", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView psl_update_1(@ModelAttribute(value = "ps")  ProductSale ps,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** ps lineItem edit update ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}

			
		ps.setAction("edit_update");
		ps.setLaction("edit_update_lineitem");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
			psService.updatePsl1(ps.getLineitem());
			ps.setLineitem(new ProductSaleLineItem());
			ps.setLaction("edit_update_insert");
			
			//reset line item action
		}
		
		
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.setStdename("PRODUCT SALE");  
		ps.setLineitem(new ProductSaleLineItem());
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setMyaction("edit");
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		//setListBoxValues(mv, request);
		return mv;
	}
	@RequestMapping(value="/ps/deletel", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView psl_delete(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
	
String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		else if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}
			
		String pslnid =request.getParameter("pslnid");
		logger.info("*** ps deletel ***:"+ps.getSaletype());
		
		psService.deletePsl(pslnid);
		
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		ps.setLineitem(new ProductSaleLineItem());
		ps.setLaction("insert");//reset line item action
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
	
		ps.setStdename("PRODUCT SALE");  
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setMyaction("insert");//reset line item action
	
		
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		//setListBoxValues(mv, request);
		
		return mv;
	}
	@RequestMapping(value="/ps/deletel_1", method = RequestMethod.POST)
	public ModelAndView psl_delete1(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
	String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}
			
		String pslnid =request.getParameter("pslnid");
		logger.info("*** ps delete ***:"+pslnid);
		
		psService.deletePsl1(pslnid);
		
		mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		ps.setLineitem(new ProductSaleLineItem());
		ps.setAction("edit_update");
		ps.setLaction("edit_update_insert");
		ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.setStdename("PRODUCT SALE");  
		
		ps.getLineitem().setStname((String)request.getSession().getAttribute("sessionUStname"));
		ps.getLineitem().setStnid((String)request.getSession().getAttribute("sessionUStnid"));
		mv.addObject("ps",ps);
		mv.addObject("uomlist", prService.getAllUomList() );
		//setListBoxValues(mv, request);
		
		return mv;
	}
	
	@RequestMapping(value="/ps/delete", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView ps_delete(ProductSale ps,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("ps_list");
					
		String psnid =request.getParameter("psnid");
		logger.info("*** ps delete ***:"+psnid);
		
		psService.deletePs(psnid);
		
		mv.addObject("pslist",psService.getPsSearchRecords(ps,request));
		//mv.addObject("pagination", page);
		mv.addObject("ps", ps);
		
		return mv;
	}
	
	@RequestMapping(value="/ps/delete1", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView ps_delete1(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request,BindingResult result) {
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
	
			mv = new ModelAndView("ps_list");
			
		String psnid =request.getParameter("psnid");
		String delreason =request.getParameter("delreason");
		ps.setPsnid(psnid);
		ps.setAction("delete_ps");
		ps.setDelreason(delreason);
		logger.info("*** ps delete ***:"+ps.getPsnid());
		 psvalidator.validate(ps, result);
		 
			if (!result.hasErrors()) {
				String activatekot=(String)request.getSession().getAttribute("sessionActivekot");
				
				
	     	psService.deletePs1(psnid,delreason,activatekot);
	       
			}
			
			mv.addObject("pslist",psService.getPsSearchRecords(ps,request));
			//mv.addObject("pagination", page);
			mv.addObject("ps", ps);
			
			return mv;
		
	}
	@RequestMapping(value="/ps/delete1fromps", method = RequestMethod.GET)
	public ModelAndView ps_delete1fromps(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request,BindingResult result) {
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
	String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		
		
		if(sessionsaletype.equals("inpos")){
			mv = new ModelAndView("ps_add_cou_smpl");
		}
		
		if(sessionsaletype.equals("trnspot")) {
			mv = new ModelAndView("ps_add_cou_transport");
			 mv.addObject("tripList", trpDao.getTripList((String)request.getSession().getAttribute("sessionUStname")) );
	        	
	        }
		else {
			mv = new ModelAndView("ps_add_cou");
			}
		String psnid =request.getParameter("psnid");
		ps.setPsnid(psnid);
		ps.setAction("delete_ps");
		logger.info("*** ps delete ***:"+ps.getPsnid());
		 psvalidator.validate(ps, result);
			if (!result.hasErrors()) {
	     	psService.deletePs(psnid);
	        mv =	new ModelAndView("redirect:/ps/counter");
			}
			ps.setFromdate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
			ps.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
			mv.addObject("pslist",psService.getPsAllRecords(ps,request));
			mv.addObject("ps",ps);
			mv.addObject("uomlist", prService.getAllUomList() );
			return mv;
	}
	@RequestMapping(value="ps/list", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView ps_list(ProductSale ps, HttpServletRequest request) {
		
		
	
		logger.info("***ps list  ***:"+ps.getStname());
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		SaleClosing scl1 =new SaleClosing();

		scl1.setStname(	(String)request.getSession().getAttribute("sessionUStname"));
		scl1.setStdename((String)request.getSession().getAttribute("sessionUStdename"));
		SaleClosing scl= sclDao.getLastClosingTimeDate(scl1);
		
		if(scl == null){
			
			
		ps.setFromdate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
		ps.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
		ps.setShiftfrom("00:00:00");
		ps.setShiftto(CalendarUtil.getTimeNow()) ;
		}
		else{
			ps.setStname(	(String)request.getSession().getAttribute("sessionUStname"));
			ps.setStdename((String)request.getSession().getAttribute("sessionUStdename"));
			
			ps.setFromdate(scl.getScldateto());
			ps.setShiftfrom(scl.getSclshiftto());
			ps.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
			ps.setShiftto(CalendarUtil.getTimeNow()) ;
		}
		mv = new ModelAndView("ps_list");
		
		 String sessionsaletype=(String) request.getSession().getAttribute("sessionSaletype");
		 if(sessionsaletype.equals("trnspot")) {
			 String psvno=request.getParameter("psvno");
			 String printstatus=request.getParameter("printstatus");
				String printid=request.getParameter("printid");
				String psdate=request.getParameter("psdate");
				if(psdate !=null) {
					ps.setTodate(psdate);
				}
if(psvno !=null && ! psvno.equals("")) {
	ps.setFiltertype("voucherno");
	ps.setFiltervalue1(psvno);
}
		if (printstatus!=null) {
			mv.addObject("printstatus", printstatus);

		}
		if (printid!=null) {
			mv.addObject("printid", printid);

		}
		 }
		//Pagination page = new Pagination();
	//	page.setCount(psService.getPsAllCount(request));
		//RowBounds rowbounds = new RowBounds(page.getStart(), page.getLimit());
		mv.addObject("pslist",psService.getPsSearchRecords(ps,request));
		//mv.addObject("pagination", page);
		mv.addObject("ps", ps);
		
		return mv;
	}
	
	Pagination page1 = new Pagination();
	@RequestMapping(value = "/ps/sedata", method = RequestMethod.GET)
	public ModelAndView ps_sedata( @ModelAttribute(value = "ps")ProductSale ps, HttpServletRequest request) {
		
		logger.info("***cons sedata ***: sename:"+ps.getSename()+"seaction:"+ps.getSeaction());
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		mv = new ModelAndView("ps_list");
	    //ps.setSename(ps.getSename()+"%");
	    String filtertype =	request.getParameter("filtertype");
	    String fromdate =	request.getParameter("fromdate");
	    String todate =	request.getParameter("todate");
	    String filtervalue1 =	request.getParameter("filtervalue1");
	    String stname =	request.getParameter("stname");
	    String paymenttype =	request.getParameter("paymenttype");
	    
	    ps.setFiltertype(filtertype);
	    ps.setFromdate(fromdate);
	    ps.setTodate(todate);
	    ps.setFiltervalue1(filtervalue1);
	    ps.setStname(stname);
	    ps.setPaymenttype(paymenttype);
	    String saletype =	request.getParameter("saletype");
	    ps.setSaletype(saletype);
	    
	    String shiftfrom =	request.getParameter("shiftfrom");
	    String shiftto =	request.getParameter("shiftto");
	    ps.setShiftfrom(shiftfrom);
	    ps.setShiftto(shiftto);
	    System.out.println(fromdate+" "+todate +""+saletype);
	    System.out.println(shiftfrom+" "+shiftto);
		//page1.setCount(psService.getPsSearchCount(ps,request));
		//page1.getPage(ps.getSeaction(), page1);
		
	    
		//RowBounds rowBounds = new RowBounds(page1.getStart(),page1.getLimit());
	    
	   
		mv.addObject("pslist", psService.getPsSearchRecords(ps,request));
		//mv.addObject("pagination", page1);
	
		return mv;
	}
	@RequestMapping(value="ps/list/filter_nt/search",method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView p_list_filter_nt_search(@ModelAttribute(value = "ps")  ProductSale ps,
			HttpServletRequest request,BindingResult result) {
		logger.info("*** ps filter nt search ***filter value1::"+ps.getFromdate());
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("ps_list_filter_nt");
		ps.setAction("re_search");
	//	pValidator.validate(p, result);
		
		if (!result.hasErrors()) {
			if(!ps.getFiltertype().isEmpty())
			setPsListFilterNtSearchValues(mv, ps,request);
			//mv.addObject("tslist", tsService.getTsregRecords(ts));
		}
	
		////setListBoxValues(mv, request);
		mv.addObject("ps", ps);
		return mv;
	}
	
	
	
	
	@RequestMapping(value="ps/list/filter_nt/export",method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView p_list_filter_nt_export(@ModelAttribute(value = "ps")  ProductSale ps,
			HttpServletRequest request,BindingResult result) {
		logger.info("*** ps filter nt search ***filter value1::");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		
		
		mv = new ModelAndView("ps_list_filter_nt");
		ps.setAction("re_search");
	//	pValidator.validate(p, result);
		
		if (!result.hasErrors()) {
			ps.setAction("export");
			if(ps.getFiltertype().equals("prname") || ps.getFiltertype().equals("prcode")) {
				System.out.println(ps.getFiltertype()+""+ps.getFiltertype());
			String prnid	=prService.getprnidByprcodeorname(ps.getFiltertype(),ps.getFiltervalue1());
			ps.setFilterprnid(prnid);
			}
			
			ps.setPslmap(psService.getPsSearchRecords2(ps,request));
			request.setAttribute("reportname","Sale Register.xls");
			//mv.addObject("tslist", tsService.getTsregRecords(ts));
		}
	
		////setListBoxValues(mv, request);
		mv.addObject("ps", ps);
		return mv;
	}

	
	@RequestMapping(value = "/ps/export", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView ps_export( @ModelAttribute(value = "ps")ProductSale ps, HttpServletRequest request) {
		
		logger.info("***cons sedata ***: sename:"+ps.getSename()+"seaction:"+ps.getSeaction());
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		mv = new ModelAndView("ps_list");
		  String filtertype =	request.getParameter("filtertype");
		    String fromdate =	request.getParameter("fromdate");
		    String todate =	request.getParameter("todate");
		    String filtervalue1 =	request.getParameter("filtervalue1");
		    String stname =	request.getParameter("stname");
		    String paymenttype =	request.getParameter("paymenttype");
		    
		    ps.setFiltertype(filtertype);
		    ps.setFromdate(fromdate);
		    ps.setTodate(todate);
		    ps.setFiltervalue1(filtervalue1);
		    ps.setStname(stname);
		    ps.setPaymenttype(paymenttype);
		    String saletype =	request.getParameter("saletype");
		    ps.setSaletype(saletype);
		    
		    String shiftfrom =	request.getParameter("shiftfrom");
		    String shiftto =	request.getParameter("shiftto");
		    ps.setShiftfrom(shiftfrom);
		    ps.setShiftto(shiftto);
	    ps.setAction("export");
		mv.addObject("pslist", psService.getPsSearchRecords(ps,request));
		request.setAttribute("reportname","Sale Register.xls");
		mv.addObject("ps", ps);

		
		
		return mv;
	}
	
	@RequestMapping(value="ps/{id}/nt_search", method = RequestMethod.GET)
	public ModelAndView ps_nt_search( @PathVariable("id") String id, HttpServletRequest request) {
	
		logger.info("***nt search  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		return mv;
	}
/*	@RequestMapping(value = "ps/psreq/pending", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView ps_reqpend( @ModelAttribute(value = "ps")ProductSale ps, HttpServletRequest request) {
		logger.info("*** psreq pending  ***:pt");
		ModelAndView mv = new ModelAndView("ps_add_cons");
		
		
		mv.addObject("psrequests", psService.getPsreqRecords2());
		logger.info("*** psreq pending  ***:ptww"+psService.getPsreqRecords2());
		mv.addObject("ps", ps);
		return mv;
	}*/

	@RequestMapping(value = "ps/printstanderd", method = RequestMethod.GET)
	public ModelAndView printstanderd(HttpServletRequest request) throws Exception {
	
		logger.info("ps voucher print:");
		String psnid = request.getParameter("psnid");
		String pslnettotal2 = request.getParameter("pslnettotal2");
		ProductSale ps = new ProductSale();
		 
		//set purchase
		ps = psService.getPsRecord(psnid);
		if(pslnettotal2!=null){
		ps.setPslnettotal2(new BigDecimal(pslnettotal2));
		}
	
				ps.setPslineitems(psService.getPslRecords(psnid));
		
		Store st=stDao.getStRecord1(ps.getStname());
		ps.setHeadname(st.getCname());
		ps.setHeadaddress(st.getAddress());
		ps.setFooter(st.getFooter());
		/*
		 * String tarnslate=Translator.execute("Hello!",
		 * Language.ENGLISH,Language.ARABIC); System.out.println(tarnslate);
		 */
		Customer cu=cuDao.getCuRecord1(ps.getCuname());
if(cu!=null){
	ps.setCutrnumber(cu.getTaxregno());

}

		
		
		String a4model= "";
		String a4printtype=(String)request.getSession().getAttribute("sessiona4printer");
		
		if(a4printtype.equals("model1")) {
			System.out.println(a4printtype);

			a4model="ProductSaleReceipt";
		}
		else if(a4printtype.equals("model2")) {
			a4model="ProductSaleReceiptModel2";
		}
		else if(a4printtype.equals("model3")) {
			a4model="ProductSaleReceiptModel3";
		}
		else if(a4printtype.equals("model4")) {
			
			a4model="ProductSaleReceiptModel4";
		}
		else if(a4printtype.equals("model5")) {
			a4model="ProductSaleReceipt5Pdf";
		}
		else if(a4printtype.equals("model6")) {
			if(ps.getTickettype() !=null && ps.getTickettype().equals("pass")) {
			a4model="Productsalepasangerticket";}
			else if(ps.getTickettype() !=null && ps.getTickettype().equals("parcel")) {
				a4model="ProductsalePrintParcel";
				System.out.println(a4model);
			}
			else {
				a4model="ProductSaleReceipt";
			}
		}
		
		
		
		return new ModelAndView(a4model,"productsale", ps);
	}
	@RequestMapping(value = "ps/printthermal", method = RequestMethod.GET)
	public ModelAndView printthermal(HttpServletRequest request) {
		logger.info("ps voucher print:");
		String psnid = request.getParameter("psnid");
System.out.println("bbb"+psnid);
/*		String pslnettotal2 = request.getParameter("pslnettotal2");
		
		String cashpaid = request.getParameter("cashpaid");
		String crdlnettotal = request.getParameter("crdlnettotal");
		String received = request.getParameter("received");
		String balance = request.getParameter("balance");
		String pslnettotal = request.getParameter("pslnettotal");
		String otherdiscount = request.getParameter("otherdiscount");
		String psltotdisprice2 = request.getParameter("psltotdisprice2");
*/
		
		ProductSale ps = new ProductSale();
		     
		//set purchase
		ps = psService.getPsRecord(psnid);
		
	/*	if(pslnettotal2!=null){
		ps.setPslnettotal2(new BigDecimal(pslnettotal2));
		}
		if(cashpaid!=null){

		ps.setCashpaid(new BigDecimal(cashpaid));
		}
		if(crdlnettotal!=null){

		ps.setCrdlnettotal(new BigDecimal(crdlnettotal));
		}
		if(received!=null){

		ps.setReceived(Double.valueOf(received));
		}
		if(balance!=null){

		ps.setBalance(Double.valueOf(balance));
		}
		if(pslnettotal!=null){

		ps.setPslnettotal(new BigDecimal(pslnettotal));
		}
		if(otherdiscount!=null){

			ps.setOtherdiscount(new BigDecimal(otherdiscount));
			}
		if(psltotdisprice2!=null){   

			ps.setPsltotdisprice2(new BigDecimal(psltotdisprice2));
			}*/
		
		
		//getMaxvno(ps);
System.out.println("ps.getCashpaid()"+ps.getCashpaid());
	if(ps.getPsstatus().equals("Deleted"))	{
      ps.setPslineitems(psService.getPslRecordsDeleted(psnid));
	}
	else{
		ps.setPslineitems(psService.getPslRecords(psnid));
	}
		Store st=stDao.getStRecord1(ps.getStname());
		ps.setHeadname(st.getCname());
		ps.setHeadaddress(st.getAddress());
		
		
		String tmodel= "";
		String tprinttype=(String)request.getSession().getAttribute("sessionTprinter");
		
		if(tprinttype.equals("model1")) {
			tmodel="ProductSaleReceiptPdf";
		}
		else if(tprinttype.equals("model2")) {
			tmodel="ProductSaleReceipt2Pdf";
		}
		else if(tprinttype.equals("model3")) {
			tmodel="ProductSaleReceipt3Pdf";
		}  
		
		else if(tprinttype.equals("model4")) {
			tmodel="ProductSaleTReceipt4Pdf";
		}  
		return new ModelAndView(tmodel,"productsale", ps);

	}
	private void getMaxvno(ProductSale ps) {
		Integer max = psDao.getMaxvno();
		logger.info("max :"+max);
		
		if(max!=null){
			max = max + 1;
		}
		else max = Integer.valueOf(Constants.START_NUMBER);
			ps.setPsvno(Constants.PREFIX_PRODUCT_SALES+max);		
	}

	@RequestMapping(value = "ps/printt", method = RequestMethod.GET)
	public ModelAndView printt(HttpServletRequest request) throws DocumentException, IOException, PrinterException {
		logger.info("ps voucher print:");
		String psnid = request.getParameter("psnid");
		String pslnettotal2 = request.getParameter("pslnettotal2");
		ProductSale ps = new ProductSale();
		
		//set purchase
		ps = psService.getPsRecord(psnid);
		if(pslnettotal2!=null){
		ps.setPslnettotal2(new BigDecimal(pslnettotal2));
		}
		
		Store st=stDao.getStRecord1(ps.getStname());
		ps.setHeadname(st.getCname());
		ps.setHeadaddress(st.getAddress());
		
		//ps.setPslineitems(psService.getPslRecords(psnid));
		psprintService.buildCounterPdfDocument(ps, psService.getPslRecords(psnid), request);
		ModelAndView mv = new ModelAndView("redirect:/ps/list");	
        return mv;
	}
	
	@RequestMapping(value = "/ps/select/prbatch", method = RequestMethod.POST)
	public ModelAndView ps_select_prbatch(@ModelAttribute(value = "ps") ProductSale ps,
		 HttpServletRequest request,BindingResult result) {
		
		logger.info("*** select product batch***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("ps_add_cou");
			
		
		ps.setAction("select_prbatch");
		psvalidator.validate(ps, result);
		
		if (!result.hasErrors()) {
			psService.getStoreProduct(ps);
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		else{
			mv.addObject("pslineitems",psService.getPslRecords(ps.getPsnid()));
		}
		mv.addObject("uomvallist", prService.getUomListByPrbAndPr(ps.getLineitem().getBunit(),ps.getLineitem().getPrdefaultunit(),ps.getLineitem().getPrnid()) );
		mv.addObject("taxlist", taxService.getTaxRateList() );
		mv.addObject("uomlist", prService.getAllUomList() );
		mv.addObject("ps",ps);
		////setListBoxValues(mv, request);
		return mv;
	}
	
	
	@RequestMapping(value = "/ps/psreq/pending", 	 headers="Accept=*/*")
	public @ResponseBody ModelAndView psreq_pending(@ModelAttribute(value = "ps")ProductSale ps,HttpServletRequest request) {
		logger.info("*** phsreq pending  ***:pt");
		ModelAndView mv = new ModelAndView("ps_add_cons");
		
		
		mv.addObject("cophsrequests", aprqService.getPsCoureqRecords((String)request.getSession().getAttribute("sessionUtype"),(String)request.getSession().getAttribute("sessionUStname")));
		System.out.println("req"+aprqService.getPsCoureqRecords((String)request.getSession().getAttribute("sessionUtype"),(String)request.getSession().getAttribute("sessionUStname")));
		mv.addObject("ps", new ProductSale());

		return mv;
	}
	
	@RequestMapping(value="/psale/report",method=RequestMethod.GET)
	public ModelAndView psreg_re(HttpServletRequest request) {
		logger.info("***  sale register report ***");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		String type = request.getParameter("type");
		/*if(id.equals("1")){	*/
			ProductSaleLineItem psl = new ProductSaleLineItem();
			logger.info("***  sale register report ***");
			mv = new ModelAndView("psale_report");
			psl.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
			psl.setFromdate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
			psl.setGroupby(type);
			
			psl.setFromtime("00:00:00");
			psl.setTotime(""+CalendarUtil.getTimeNow());
			SaleClosing scl1 =new SaleClosing();

			scl1.setStname(	(String)request.getSession().getAttribute("sessionUStname"));
			scl1.setStdename((String)request.getSession().getAttribute("sessionUStdename"));
			SaleClosing scl= sclDao.getLastClosingTimeDate(scl1);
			if(scl == null){
				
				psl.setStname((String)request.getSession().getAttribute("sessionUStname"));
				psl.setStdename((String)request.getSession().getAttribute("sessionUStdename"));
				
				psl.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
				psl.setFromdate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
				psl.setGroupby(type);
				
				psl.setFromtime("00:00:00");
				psl.setTotime(""+CalendarUtil.getTimeNow());
			}
			else{
				psl.setStname(	(String)request.getSession().getAttribute("sessionUStname"));
				psl.setStdename((String)request.getSession().getAttribute("sessionUStdename"));
				
				psl.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
				psl.setFromdate(scl.getScldateto());
				psl.setGroupby(type);
				
				psl.setFromtime(scl.getSclshiftto());
				psl.setTotime(""+CalendarUtil.getTimeNow());
				
				
			}
			String sessionsaletype=(String)request.getSession().getAttribute("sessionSaletype");
			if(sessionsaletype.equals("trnspot")) {
				 mv.addObject("tripList", trpDao.getTripList(psl.getStname()));
		        	
		        }
			mv.addObject("psl", psl);
			/*setListBoxValues(mv, request);*/
		/*}*/
	
		return mv;
	}
	@RequestMapping(value="/psale/search",method={RequestMethod.POST, RequestMethod.GET})
	public ModelAndView psreg_re1_search(@ModelAttribute(value = "psl")  ProductSaleLineItem psl,
			HttpServletRequest request,BindingResult result) {
		logger.info("***  sale register report search ***");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("psale_report");
		psl.setAction("re_search");
	/*	psregvalidator.validate(psreg, result);
		
		if (!result.hasErrors()) {*/
			setPsRegRe1SearchValues(mv, psl,request.getParameter("type"));
		/*}*/
	
		/*setListBoxValues(mv, request);*/
		mv.addObject("psl", psl);
		String sessionsaletype=(String)request.getSession().getAttribute("sessionSaletype");
		if(sessionsaletype.equals("trnspot")) {
			 mv.addObject("tripList", trpDao.getTripList((psl.getStname())));
	        	
	        }
		return mv;
	}
	
	
	
	
	
	@RequestMapping(value="/psreg1/re1/export",method=RequestMethod.POST)
	public ModelAndView psreg_re1_export(@ModelAttribute(value = "psl")  ProductSaleLineItem psl,
			HttpServletRequest request, BindingResult result) {
		logger.info("***  sale register export ***psvno:");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("psale_report");
		
			psl.setAction("export");
		
			setPsRegRe1SearchValues(mv, psl,request.getParameter("type"));
			request.setAttribute("reportname","ProductSaleRegister.xls");
		
		//setListBoxValues(mv, request);
		return mv;
	}
	
	@RequestMapping(value="/psreg1/re1/exportpdf",method= {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView psreg_re1_export_pdf(@ModelAttribute(value = "psl")  ProductSaleLineItem psl,
			HttpServletRequest request, BindingResult result) {
		logger.info("***  sale register export ***psvno:");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("psale_report");
		
	
		
			//setPsRegRe1SearchValues(mv, psl,request.getParameter("type"));
			if(psl.getTrpname() !=null) {
			TripMaster trp=	trpDao.getDetailsByTripName(psl.getTrpname() );
			if(trp!=null) {
				psl.setDrivername(trp.getDrivername());
				psl.setDrivernumber(trp.getDrivernumber());
				psl.setTripendtime(trp.getTripendtime());
				psl.setTripstarttime(trp.getTripstarttime());
				psl.setFrmlocation(trp.getFrmlocation());
			}
			}
			psl.setPslliItems(psService.getPsregSearchRecords1(psl));
			return new ModelAndView("PsaleReoprtPdf","productsaleline", psl);
	}
	
	
	
	
	public void setPsRegRe1SearchValues(ModelAndView mv, ProductSaleLineItem psl, String type){
		
		
	String prname= psl.getPrname();
		if(psl.getPrname()!=null&&!psl.getPrname().isEmpty()) {
	
		if(psl.getPrname().substring(psl.getPrname().length() - 1).equals(",")) {
		psl.setPrname(psl.getPrname().substring(0, psl.getPrname().length()-1));
		
		//psl.setPrname(psl.getPrname().replace(",", "','"));
		}
		
		}
		if(type.equals("chart")) {
		Gson gson = new Gson();
		
		mv.addObject("records", gson.toJson(psService.getPsregSearchRecords1(psl)));
		}
		else {
			mv.addObject("records",psService.getPsregSearchRecords1(psl));
		}
		
		
		ProductSaleReturnLineItem r1 =new ProductSaleReturnLineItem();
		if(type.equals("chart")) {
			Gson gson = new Gson();
			
		mv.addObject("records1", gson.toJson(psrDao.getPsrRegSearchRecords1(psl)));
		}
		else {
			mv.addObject("records1", psrDao.getPsrRegSearchRecords1(psl));
		}
		
		mv.addObject("rl", r1);
		psl.setPrname(prname);
		mv.addObject("type", type);
		mv.addObject("psl", psl);
	}
	
	
	@RequestMapping(value="/ps/analysis", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView psl_analysis(@ModelAttribute(value = "ps") ProductSale ps,HttpServletRequest request) {
		logger.info("*** supplier edit ***:");
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
			mv = new ModelAndView("psreg_report");
			ps.setStnid((String)request.getSession().getAttribute("sessionUStnid"));
			ps.setStname((String)request.getSession().getAttribute("sessionUStname"));
			ps.setLineitem(new ProductSaleLineItem());
			SaleClosing scl1 =psService.getLastClosingTimeDate(ps);
			if(scl1!=null){
				
				ps.setFromdate(scl1.getScldateto());
				ps.setFromtime(scl1.getSclshiftto());
				ps.setTotime(""+CalendarUtil.getTimeNow());
				ps.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));

			}
			else{
				ps.setFromdate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
				ps.setTodate(CalendarUtil.getMySQLDateFromString(CalendarUtil.getDateToday()));
			
				ps.setFromtime(""+CalendarUtil.getTimeNow());
			
			}
		mv.addObject("ps",ps);
		//setListBoxValues(mv, request);
		return mv;
	}

	@RequestMapping(value="/phsreg/re1/search1",method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView phsreg_re1_search1(@ModelAttribute(value = "ps")  ProductSale ps,
			HttpServletRequest request,BindingResult result) {
		logger.info("*** pharmacy sale register report search ***");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("psreg_report");
	
		ProductSaleLineItem psl=new ProductSaleLineItem();
		psl.setFromdate(ps.getFromdate());
		psl.setFromtime(ps.getFromtime());
		psl.setTodate(ps.getTodate());
	psl.setTotime(ps.getTotime());
		System.out.println(ps.getLineitem().getPrname());
			if(ps.getFiltertype().equals("month")){
			
				setPhsRegRe1SearchValues3(mv, psl);
			}
			
		else if(ps.getFiltertype().equals("year")){
			
									
				setPhsRegRe1SearchValues4(mv, psl);
			}
		else if(ps.getFiltertype().equals("day")){
	
				setPhsRegRe1SearchValues5(mv, psl);
			}
			else if(ps.getFiltertype().equals("hour")){
				setPhsRegRe1SearchValues6(mv, psl);
			}
			
	/*	} */
	
		/*setListBoxValues(mv, request);*/
		mv.addObject("ps", ps);
		return mv;
	}
	
	
	@RequestMapping(value="ps/updatedis/status", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView updatedis(ProductSale ps, HttpServletRequest request) {
		
		logger.info("*** updatestatus  ***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
			mv = new ModelAndView("ps_add_cou");
			String pslnid = request.getParameter("pslnid");
			String prnid = request.getParameter("prnid");
			String nondiscount = request.getParameter("nondiscount");
			Boolean discount=Boolean.valueOf(nondiscount);
			psService.updateDisStatus(pslnid,discount,prnid);
		
		mv.addObject("ps", ps);
		return mv;
	}
	

	@RequestMapping(value = "ps/printtdir", method = RequestMethod.GET)
	public ModelAndView printtdir(HttpServletRequest request) throws DocumentException, IOException, PrinterException, PrintException {
		logger.info("direct voucher print:");
		String psnid = request.getParameter("psnid");
		String pslnettotal2 = request.getParameter("pslnettotal2");
		ProductSale ps = new ProductSale();
		
		//set purchase
		ps = psService.getPsRecord(psnid);
		if(pslnettotal2!=null){
		ps.setPslnettotal2(new BigDecimal(pslnettotal2));
		}
		
		Store st=stDao.getStRecord1(ps.getStname());
		ps.setHeadname(st.getCname());
		ps.setHeadaddress(st.getAddress());
		
		
		String printtype=(String)request.getSession().getAttribute("sessionTprinter");
		 if(printtype.equals("model1")){
			psprintService.buildCounterPdfDocument1(ps, psService.getPslRecords(psnid), request);
			}
		else if(printtype.equals("model2")){
		psprintService.buildCounterPdfDocument2(ps, psService.getPslRecords(psnid), request);
		}
		else if(printtype.equals("model3")){
			psprintService.buildCounterPdfDocument3(ps, psService.getPslRecords(psnid), request);
		}
		else if(printtype.equals("model4")){
			
			psprintService.buildCounterPdfDocument4(ps, psService.getPslRecords(psnid), request);
		}
		ModelAndView mv = new ModelAndView("redirect:/ps/counter");

        return mv;
	}
	private void setPhsRegRe1SearchValues4(ModelAndView mv, ProductSaleLineItem psl) {
		mv.addObject("ps", psl);
		
		mv.addObject("records", psService.getPhsregSearchRecordsYrlyChart(psl));
		
	}

	private void setPhsRegRe1SearchValues5(ModelAndView mv, ProductSaleLineItem psl) {
		mv.addObject("ps", psl);
		Gson gson = new Gson();
		System.out.println("chartt "+gson.toJson(psService.getPhsregSearchRecordsDayly(psl)));
		mv.addObject("records", gson.toJson(psService.getPhsregSearchRecordsDayly(psl)));
		
	}
	public void setPhsRegRe1SearchValues3(ModelAndView mv, ProductSaleLineItem ps){

		mv.addObject("ps", ps);
		mv.addObject("records", psService.getPhsregSearchRecordsMnthly(ps));
		
	}
	private void setPhsRegRe1SearchValues6(ModelAndView mv, ProductSaleLineItem ps) {
		mv.addObject("ps", ps);
		mv.addObject("records", psService.getPhsregSearchRecordsHourly(ps));
		
	}
	
	
/*	@RequestMapping(value = "ps/select/prbatch", method = RequestMethod.POST)
	public ModelAndView sttr_select_prbatch(@ModelAttribute(value = "sttr") StockTransfer sttr,
		 HttpServletRequest request,BindingResult result) {
		
		logger.info("*** select product batch***:");
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		mv = new ModelAndView("sttr_add");
		
		sttr.setAction("select_prbatch");
		sttrvalidator.validate(sttr, result);
		
		if (!result.hasErrors()) {
			sttrService.getProductBatch(sttr);
		}
		else{
			mv = new ModelAndView("sttr_add");
		}
		setValues(mv,sttr);
		return mv;
	}*/
	
	
	
	
	/*
	public void setValuesWithLineItems(ModelAndView mv, Purchase p, PurchaseLineItem pl){
		mv.addObject("purchase", p);
		mv.addObject("purchaselineitem",pl);
		mv.addObject("purchaselineitemslist",pService.getPlRecords(p.getPnid()) );
	}*/
	
	
	/*public void setListBoxValues(ModelAndView mv, HttpServletRequest request) {
		mv.addObject("countrylist", listboxService.getCAllRecords());
		  mv.addObject("storelist", listboxService.getStAllRecords());
			mv.addObject("genderlist", patientService.getAllListBoxByName("gender"));
			mv.addObject("locationlist", patientService.getAllListBoxByName("location"));
			mv.addObject("maritalstatuslist", patientService.getAllListBoxByName("marital status"));
			mv.addObject("bloodgrouplist", patientService.getAllListBoxByName("blood group"));
			model.addObject("countrylist", patService.getCAllRecords());
			 
	}
	*/
	public void setEnteredByValues(HttpServletRequest request, ProductSale ps) {
		ps.setEnteredby(SessionController.getUserName(request));
		ps.setLastmodifiedby(SessionController.getUserName(request));
	}
	
	public void setEnteredByValuesForDE(HttpServletRequest request, DeliveryOrder de) {
		de.setEnteredby(SessionController.getUserName(request));
		de.setLastmodifiedby(SessionController.getUserName(request));
	}
	public void setPsListFilterNtSearchValues(ModelAndView mv, ProductSale ps,HttpServletRequest request){
		//ReportPagination repage = new ReportPagination();
	    
		//repage.setCount(psService.getPsSearchCount2(ps));
	//	RowBounds rowbounds = new RowBounds(repage.getStart(), repage.getLimit());
		System.out.println(ps.getFiltervalue1());
		System.out.println(ps.getFiltertype());
		
		if(ps.getFiltertype().equals("prname") || ps.getFiltertype().equals("prcode")) {
			System.out.println(ps.getFiltertype()+""+ps.getFiltertype());
		String prnid	=prService.getprnidByprcodeorname(ps.getFiltertype(),ps.getFiltervalue1());
		ps.setFilterprnid(prnid);
		}
		
		ps.setPslmap(psService.getPsSearchRecords2(ps,request));
		
		mv.addObject("ps", ps);
		//mv.addObject("pagination", repage);
	}
	
	@RequestMapping(value = "ps/job", method = RequestMethod.POST)
	public ModelAndView ps_job(@ModelAttribute(value = "job")  JobCard job,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** product sles from   ***:");
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		job.setEnteredby(SessionController.getUserName(request));
		job.setLastmodifiedby(SessionController.getUserName(request));
		/*
		ProductSale ps = new ProductSale();
		ps.setDenid(de.getDenid());
		ps.setAction("delivery");
        psvalidator.validate(ps, result); */
        
        mv = new ModelAndView("ps_list");
        
        psService.batchInsertjob(job);
        mv = new ModelAndView("job_inv_add");
        job.setStatus("saved");
        mv.addObject("job",job);
        return mv;
	}
	
	@RequestMapping(value = "ps/print", method = RequestMethod.GET)
	public ModelAndView print(HttpServletRequest request) {
		logger.info("ps voucher print:");
		String psnid = request.getParameter("psnid");
		String pslnettotal2 = request.getParameter("pslnettotal2");
		ProductSale ps = new ProductSale();
		 
		//set purchase
		ps = psService.getPsRecord(psnid);
		if(pslnettotal2!=null){
		ps.setPslnettotal2(new BigDecimal(pslnettotal2));
		}
	
				ps.setPslineitems(psService.getPslRecords(psnid));
		
		Store st=stDao.getStRecord1(ps.getStname());
		ps.setHeadname(st.getCname());
		ps.setHeadaddress(st.getAddress());
		ps.setFooter(st.getFooter());
		
		Customer cu=cuDao.getCuRecord1(ps.getCuname());
if(cu!=null){
	ps.setCutrnumber(cu.getTaxregno());

}

		
		
		String a4model= "";
		String a4printtype=(String)request.getSession().getAttribute("sessiona4printer");
		
		if(a4printtype.equals("model1")) {
			System.out.println(a4printtype);

			a4model="ProductSaleReceipt";
		}
		else if(a4printtype.equals("model2")) {
			a4model="ProductSaleReceiptModel2";
		}
		else if(a4printtype.equals("model3")) {
			a4model="ProductSaleReceiptModel3";
		}
		else if(a4printtype.equals("model4")) {
			a4model="ProductSaleReceiptModel4";
		}
		
		else if(a4printtype.equals("model5")) {
			a4model="ProductSaleReceipt5Pdf";
		}
		return new ModelAndView(a4model,"productsale", ps);
	}
	
	
	@RequestMapping(value = "rentel/postdetl", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView postRentelToSale(@ModelAttribute(value = "rentel")  RentelDetailsLineitem renteldetails,BindingResult result, HttpServletRequest request) {
		
		logger.info("*** product sles from   ***:");
		//this will be redirected if the page refreshed or clicked two times
		if(! isTokenValid(request)){
			return new ModelAndView("redirect:/ps");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		
		
		String rentlnid=request.getParameter("rentlnid");
		
		String rentdate=request.getParameter("rentdate");
		if(!rentdate.equals("")){
			rentService.updatePostDate(rentlnid,rentdate);
		}
		
		renteldetails =rentService.getRendetailsByRentlnId(rentlnid);
		renteldetails.setEnteredby(SessionController.getUserName(request));
		renteldetails.setLastmodifiedby(SessionController.getUserName(request));
		/*
		ProductSale ps = new ProductSale();
		ps.setDenid(de.getDenid());
		ps.setAction("delivery");
        psvalidator.validate(ps, result); */
        
        mv = new ModelAndView("ps_list");
    	String saletype=(String)request.getSession().getAttribute("sessionstoretype");
    	renteldetails.setSaletype(saletype);
        psService.batchInsertRent(renteldetails);
        mv = new ModelAndView("redirect:/ps/list");	
        return mv;
	}
	
	@RequestMapping(value="/ps/numpad", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView numpad(HttpServletRequest request) {
		logger.info("*** numpad jsp ***:");
		ModelAndView mv = new ModelAndView("not_logged");
		
		String ids=request.getParameter("id");
			mv = new ModelAndView("numpad");
			
			mv.addObject("ids",ids);
	
		//setListBoxValues(mv, request);
		return mv;
	}
	@RequestMapping(value = "ps/postosale", method ={RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_postosale(@ModelAttribute(value = "qt")  Quotation qt,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** product sles from   ***:");
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		String qtnid=request.getParameter("qtnid");
		qt=qtDao.getQtRecordpost(qtnid);
		qt.setEnteredby(SessionController.getUserName(request));
		qt.setLastmodifiedby(SessionController.getUserName(request));
		
        
        mv = new ModelAndView("ps_list");
    	String saletype=(String)request.getSession().getAttribute("sessionstoretype");
    	
       ProductSale ps= psService.batchInsertqt(qt,saletype);
   return new ModelAndView("redirect:/ps/edit?psnid="+ps.getPsnid());	
    
	}
	
	@RequestMapping(value = "ps/postosaleApp", method ={RequestMethod.POST,RequestMethod.GET})
	public ModelAndView ps_postosale_app(@ModelAttribute(value = "aprq")  AndroidProductRequest aprq,
			BindingResult result, HttpServletRequest request) {
		
		logger.info("*** product sles from   ***:");
		//this will be redirected if the page refreshed or clicked two times
		if(!isTokenValid(request)){
			return new ModelAndView("redirect:/ps");
		}
		
		ModelAndView mv = new ModelAndView("not_logged");
		if(!SessionController.isLogin(request)){
			return mv;
		}
		String aprqnid=request.getParameter("aprqnid");
		aprq=aprqDao.getAllAprqRecords(aprqnid);
		aprq.setEnteredby(SessionController.getUserName(request));
		aprq.setLastmodifiedby(SessionController.getUserName(request));
		
        AndroidUser and=auserDao.getUAndroRecord(aprq.getUnid());
       System.out.println(and.getUnid()+""+aprq.getStname());
        BeanUtils.copyProperties(and, aprq);
        mv = new ModelAndView("ps_list");
    	String saletype=(String)request.getSession().getAttribute("sessionstoretype");
    	
       ProductSale ps= psService.batchInsertaprq(aprq,saletype);
       if(aprq.getTotdisc() >0.00) {
    	  
    	   ps.setOtherdiscounttot(new BigDecimal(aprq.getTotdisc()));
    	  
    	   psService.otherDiscountUpdate(ps);
       }
       aprq.setReqstatus(Constants.STATUS_COMPLETED);
       aprqDao.updateAprqStatus(aprq);
   return new ModelAndView("redirect:/ps/edit?psnid="+ps.getPsnid());	
    
	}
	@RequestMapping(value = "ps/barprintpr", method = {RequestMethod.GET,RequestMethod.POST})
	public ModelAndView barprintpr(HttpServletRequest request) throws DocumentException, IOException, PrinterException {
		logger.info("pr  barcode print:");
		
		
		//set purchase
		String psnid = request.getParameter("psnid");
	ProductSale ps =psDao.getPsRecord(psnid);
	Store st=stDao.getStRecord1(ps.getStname());
	ps.setHeadname(st.getCname());
	ps.setHeadaddress(st.getAddress());
		
			return new ModelAndView("ProductsaleBarcodePrint","psList", ps);
		
		

	}

	@RequestMapping(value="ps/updatepaidSattus", method ={RequestMethod.POST,RequestMethod.GET})
	public ModelAndView prlist_crorupdtmasterpr(@ModelAttribute(value = "ps")ProductSale ps,HttpServletRequest request) {
		
		logger.info("*** product andro list ***:");
		
		
		 String jsonprsubcatList = "ert";
	ModelAndView	mv = new ModelAndView("pr_list_android");

String psnid=(String)request.getParameter("psnid");
String trippaystatus=(String)request.getParameter("trippaystatus");
String newst=(String)request.getParameter("stname");
System.out.println("vvv"+newst);
psService.updatepaidSattus(psnid,trippaystatus,newst);

 	mv.addObject("prlistandro", jsonprsubcatList);	
	
		return mv;
		
	}
	
	@RequestMapping(value="ps/updatedelSattus", method ={RequestMethod.POST,RequestMethod.GET})
	public ModelAndView prlist_crorudeltmasterpr(@ModelAttribute(value = "ps")ProductSale ps,HttpServletRequest request) {
		
		logger.info("*** product andro list ***:");
		
		Gson gson = new Gson();

		 String jsonprsubcatList = "ert";
	ModelAndView	mv = new ModelAndView("pr_list_android");

String psnid=(String)request.getParameter("psnid");
String deliveryprclsts=(String)request.getParameter("deliveryprclsts");
String recivelist=(String)request.getParameter("recivelist");

psService.updateDelSattus(psnid,deliveryprclsts,recivelist);

 	mv.addObject("prlistandro", jsonprsubcatList);	
	
		return mv;
		
		
	}
	
	
}
