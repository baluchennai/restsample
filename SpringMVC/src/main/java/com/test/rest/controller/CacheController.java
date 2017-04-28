package com.test.rest.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

@Controller
public class CacheController {

	/**
	 * Simple rest call method that returns sample page (Cache not enabled).
	 * 
	 * @param name input name
	 * @return
	 */
	@RequestMapping(value = "cache/{name}", method = RequestMethod.GET, produces = "application/json")
	// @Cacheable(value="basicCache")
	public ResponseEntity<String> getName(@PathVariable String name) {
		// actualData(name, model);
		System.out.println("Service is getting built.....");
		return new ResponseEntity<>("You name is: "+name,HttpStatus.OK);
	}

	/**
	 * Method that sets return response based on simple 'Cache-Control'
	 * settings. 'Cache-Control' is enabled for 'x' amount of time and expiry is
	 * automatically handled by browser and server code. Note: Doing (F5) will
	 * make a fresh call. TBD: How to simulate using Postman?
	 * 
	 * @param name input name
	 * @return
	 */
	@RequestMapping(value = "cache1/{name}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getName1(HttpServletResponse response, @PathVariable String name) {
	
		String headerValue = CacheControl.maxAge(30, TimeUnit.SECONDS).getHeaderValue();
		response.addHeader("Cache-Control", headerValue);

		return new ResponseEntity<>("You name is: "+name,HttpStatus.OK);
	}

	/**
	 * Method handles cache based on 'ETag'. ETag is checked every time to see
	 * if it matches internal logic, if matched, further calculations and logics
	 * are not executed. Also, returns response status code would be 'HTT 304
	 * Not Modified' Note: No expiry/cache duration attached to Response.
	 * 
	 * @param webRequest provides metadata informatation
	 * @param name input name
	 * @return
	 */
	@RequestMapping(value = "cache2/{name}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getName2(WebRequest webRequest, @PathVariable String name) {
		
		// 1. Do application specific log to generate eTag (with datemodified)

		if (webRequest.checkNotModified(getETag(name))) {
			//2. Verify eTag
			System.out.println("ETag Verified....");
			//3. Returns HTTP 304 Not Modified Response
			return null;
		}

		//4. Continue with further processing if eTag match fails
		System.out.println("ETag not verified, building Response...");

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Method handles cache based on 'ETag'. ETag is checked every time to see
	 * if it matches internal logic, if matched, further calculations and logics
	 * are not executed ie., the body part will not be build/returned. Also,
	 * returns response status code would be 'HTT 304 Not Modified' Note:
	 * Request header should have 'If-None-Match' matching to 'eTag'.
	 * Note: 'Cache-Control' has no meaning here, as we only bothere about eTag.
	 * 
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "cache3/{name}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getName3(@PathVariable String name) {

		String eTag = getETag(name);

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS)).eTag(eTag)
				.body("Cache Success");
	}

	/**
	 * Method handles cache based on 'ETag'. ETag is checked every time to see
	 * if it matches internal logic, if matched, further calculations and logics
	 * are not executed ie., the body part will not be build/returned. Also,
	 * returns response status code would be 'HTT 304 Not Modified' Note:
	 * Request header should have 'If-None-Match' matching to 'eTag'.
	 * 
	 * @param webRequest provides metadata informatation
	 * @param name input name
	 * @return
	 */
	@RequestMapping(value = "cache4/{name}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getName4(WebRequest webRequest, @PathVariable String name) {

		if (webRequest.checkNotModified(getETag(name), whenLastModified())) {
			System.out.println("ETag Verified....");
			return null;
		}

		System.out.println("ETag not verified, building Response...");

		String eTag = getETag(name);

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS)).eTag(eTag)
				.body("Cache Success");
	}

	private String getETagStr(String key){
		return "100";
	}

	/**
	 * Method to return eTag based on combination of:
	 * <ul>
	 * <p> key(name in this case) + current date
	 * </ul>
	 * <p> Eg: <br>
	 *  if key=name and current date=01 Feb 2017,<br>
	 *  eTag will be generated for 'Balu20170201'
	 * @param key
	 * @return
	 */
	private String getETag(String key) {

		String keyStr = (key + getCurrDate());
		System.out.println("ETag raw string: "+keyStr);
		byte[] bytes = (key + keyStr).getBytes();
		return DigestUtils.md5DigestAsHex(bytes);
	}

	/**
	 * Method to return last modified time.
	 * @return
	 */
	private long whenLastModified() {
		System.out.println(LocalDate.now());
		System.out.println(LocalDate.now().toEpochDay());
		//System.out.println(Instant.now());
		//System.out.println(Instant.now().toEpochMilli());
		return LocalDate.now().toEpochDay();
	}
	
	/**
	 * Method to compute current date in 'yyyyMMdd' format.
	 * <p>Eg: 01 Feb 2017 will be represented as 20170201.
	 * @return
	 */
	private String getCurrDate(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		return LocalDate.now().format(formatter);
		
	}

}
