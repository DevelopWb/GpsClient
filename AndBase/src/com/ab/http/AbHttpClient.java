/*
 * Copyright (C) 2012 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ab.http;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ab.global.AbAppConfig;
import com.ab.global.AbAppException;
import com.ab.http.entity.MultipartEntity;
import com.ab.http.ssl.EasySSLProtocolSocketFactory;
import com.ab.task.thread.AbThreadFactory;
import com.ab.util.AbAppUtil;
import com.ab.util.AbFileUtil;
import com.ab.util.AbLogUtil;

// TODO: Auto-generated Javadoc
/**
 * ?? 2012 amsoft.cn
 * ?????????AbHttpClient.java 
 * ?????????Http?????????
 *
 * @author ???????????????
 * @version v1.0
 * @date???2013-11-13 ??????9:00:52
 */
public class AbHttpClient {
	
    /** ?????????. */
	private static Context mContext;
	
	/** ???????????????. */
	public static Executor mExecutorService = null;
	
	/** ??????. */
	private String encode = HTTP.UTF_8;
	
	/** ????????????. */
	private String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.43 BIDUBrowser/6.x Safari/537.31";
	
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    
    /** CookieStore. */
    private CookieStore mCookieStore;  

    /** ???????????????. */
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    
    /** ????????????. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    
    /** ????????????. */
    private static final int DEFAULT_MAX_RETRIES = 2;
    
    /** ????????????. */
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    
    /** ??????. */
    protected static final int SUCCESS_MESSAGE = 0;
    
    /** ??????. */
    protected static final int FAILURE_MESSAGE = 1;
    
    /** ????????????????????????. */
    protected static final int FAILURE_MESSAGE_CONNECT = 2;
    
    /** ????????????????????????. */
    protected static final int FAILURE_MESSAGE_SERVICE = 3;
    
    /** ??????. */
    protected static final int START_MESSAGE = 4;
    
    /** ??????. */
    protected static final int FINISH_MESSAGE = 5;
    
    /** ?????????. */
    protected static final int PROGRESS_MESSAGE = 6;
    
    /** ??????. */
    protected static final int RETRY_MESSAGE = 7;
    
    /** ????????????. */
	private int mTimeout = DEFAULT_SOCKET_TIMEOUT;
	
	/** ????????????. ????????????HTTPS??????????????????SSL????????????*/
	private boolean mIsOpenEasySSL = true;
	
	/** HTTP Client*/
	private DefaultHttpClient mHttpClient = null;
	
	/** HTTP ?????????*/
	private HttpContext mHttpContext = null;
    
    /**
     * ?????????.
     *
     * @param context the context
     */
	public AbHttpClient(Context context) {
	    mContext = context;
		mExecutorService =  AbThreadFactory.getExecutorService();
		mHttpContext = new BasicHttpContext();
	}
	

	/**
	 * ?????????????????????get??????.
	 *
	 * @param url the url
	 * @param params the params
	 * @param responseListener the response listener
	 */
	public void get(final String url,final AbRequestParams params,final AbHttpResponseListener responseListener) {
		
		responseListener.setHandler(new ResponderHandler(responseListener));
		mExecutorService.execute(new Runnable() { 
    		public void run() {
    			try {
    				doGet(url,params,responseListener);
    			} catch (Exception e) { 
    				e.printStackTrace();
    			}
    		}                 
    	});      
	}
	
	/**
	 * ?????????????????????post??????.
	 *
	 * @param url the url
	 * @param params the params
	 * @param responseListener the response listener
	 */
	public void post(final String url,final AbRequestParams params,
			final AbHttpResponseListener responseListener) {
		responseListener.setHandler(new ResponderHandler(responseListener));
		mExecutorService.execute(new Runnable() { 
    		public void run() {
    			try {
    				doPost(url,params,responseListener);
    			} catch (Exception e) { 
    				e.printStackTrace();
    			}
    		}                 
    	});      
	}
	
	
	/**
	 * ???????????????get??????.
	 *
	 * @param url the url
	 * @param params the params
	 * @param responseListener the response listener
	 */
	private void doGet(String url,AbRequestParams params,AbHttpResponseListener responseListener){
		  try {
			  
			  responseListener.sendStartMessage();
			  
			  if(!AbAppUtil.isNetworkAvailable(mContext)){
				    Thread.sleep(200);
					responseListener.sendFailureMessage(AbHttpStatus.CONNECT_FAILURE_CODE,AbAppConfig.CONNECT_EXCEPTION, new AbAppException(AbAppConfig.CONNECT_EXCEPTION));
			        return;
			  }
			  
			  //HttpGet????????????  
			  if(params!=null){
				  if(url.indexOf("?")==-1){
					  url += "?";
				  }
				  url += params.getParamString();
			  }
			  HttpGet httpGet = new HttpGet(url);  
			  httpGet.addHeader(USER_AGENT, userAgent);
			  //??????
			  httpGet.addHeader(ACCEPT_ENCODING, "gzip");
			  //???????????????HttpClient
      	      HttpClient httpClient = getHttpClient();  
		      //??????HttpResponse
		      String response = httpClient.execute(httpGet,new RedirectionResponseHandler(url,responseListener),mHttpContext);  
			  AbLogUtil.i(mContext, "[HTTP GET]:"+url+",result???"+response);
		} catch (Exception e) {
			e.printStackTrace();
			//??????????????????
			responseListener.sendFailureMessage(AbHttpStatus.UNTREATED_CODE,e.getMessage(),new AbAppException(e));
		}finally{
			responseListener.sendFinishMessage();
		}
	}
	
	/**
	 * ???????????????post??????.
	 *
	 * @param url the url
	 * @param params the params
	 * @param responseListener the response listener
	 */
	private void doPost(String url,AbRequestParams params,AbHttpResponseListener responseListener){
		  try {
			  responseListener.sendStartMessage();
			  
			  if(!AbAppUtil.isNetworkAvailable(mContext)){
				    Thread.sleep(200);
					responseListener.sendFailureMessage(AbHttpStatus.CONNECT_FAILURE_CODE,AbAppConfig.CONNECT_EXCEPTION, new AbAppException(AbAppConfig.CONNECT_EXCEPTION));
			        return;
			  }
			  
			  //HttpPost????????????  
		      HttpPost httpPost = new HttpPost(url);  
		      httpPost.addHeader(USER_AGENT, userAgent);
			  //??????
		      httpPost.addHeader(ACCEPT_ENCODING, "gzip");
		      //??????????????????
		      boolean isContainFile = false;
		      if(params != null){
		    	  //??????NameValuePair?????????????????????Post????????????????????? 
			      HttpEntity httpentity = params.getEntity();
			      //??????httpRequest  
			      httpPost.setEntity(httpentity); 
			      if(params.getFileParams().size()>0){
			    	  isContainFile = true;
			      }
			  }
		      String  response = null;
		      //???????????????HttpClient
		      DefaultHttpClient httpClient = getHttpClient();  
		      if(isContainFile){
		    	  httpPost.addHeader("connection", "keep-alive");
			      httpPost.addHeader("Content-Type", "multipart/form-data; boundary=" + params.boundaryString());
		    	  AbLogUtil.i(mContext, "[HTTP POST]:"+url+",???????????????!");
		      }
		      //??????HttpResponse
		      response = httpClient.execute(httpPost,new RedirectionResponseHandler(url,responseListener),mHttpContext);  
		      AbLogUtil.i(mContext, "request???"+url+",result???"+response);
			  
		} catch (Exception e) {
			e.printStackTrace();
			AbLogUtil.i(mContext, "[HTTP POST]:"+url+",error???"+e.getMessage());
			//??????????????????
			responseListener.sendFailureMessage(AbHttpStatus.UNTREATED_CODE,e.getMessage(),new AbAppException(e));
		}finally{
			responseListener.sendFinishMessage();
		}
	}
	
	/**
     * ???????????????,???????????????????????????String??????,????????????????????????
     * @param url
     * @param params
     * @return
     */
    public void doRequest(final String url, final AbRequestParams params, final AbStringHttpResponseListener responseListener) {
    	responseListener.setHandler(new ResponderHandler(responseListener));
		mExecutorService.execute(new Runnable() { 
    		public void run() {
    			HttpURLConnection urlConn = null;
    			try {
    				responseListener.sendStartMessage();
        			
        			if(!AbAppUtil.isNetworkAvailable(mContext)){
        				Thread.sleep(200);
    					responseListener.sendFailureMessage(AbHttpStatus.CONNECT_FAILURE_CODE,AbAppConfig.CONNECT_EXCEPTION, new AbAppException(AbAppConfig.CONNECT_EXCEPTION));
    			        return;
    			    }
        			
    				String resultString = null;
					URL requestUrl = new URL(url);
					urlConn = (HttpURLConnection) requestUrl.openConnection();
			        urlConn.setRequestMethod("POST");
			        urlConn.setConnectTimeout(mTimeout);
			        urlConn.setReadTimeout(mTimeout);
			        urlConn.setDoOutput(true);
			        
					if(params!=null){
						urlConn.setRequestProperty("connection", "keep-alive");
				        urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + params.boundaryString());
						MultipartEntity reqEntity = params.getMultiPart();
				        reqEntity.writeTo(urlConn.getOutputStream());
					}else{
						urlConn.connect();
					}
			       
		            if (urlConn.getResponseCode() == HttpStatus.SC_OK){
		            	resultString = readString(urlConn.getInputStream());
		            }else{
		            	resultString = readString(urlConn.getErrorStream());
		            }
		            resultString = URLEncoder.encode(resultString, encode);
		            urlConn.getInputStream().close();
		            responseListener.sendSuccessMessage(AbHttpStatus.SUCCESS_CODE, resultString);
    			} catch (Exception e) { 
    				e.printStackTrace();
					AbLogUtil.i(mContext, "[HTTP POST]:"+url+",error???"+e.getMessage());
					//??????????????????
					responseListener.sendFailureMessage(AbHttpStatus.UNTREATED_CODE,e.getMessage(),new AbAppException(e));
    			} finally {
					if (urlConn != null)
						urlConn.disconnect();
					
					responseListener.sendFinishMessage();
				}
    		}                 
    	});      
	}
	
	
	/**
	 * ????????????????????????????????????.
	 * 
	 * @param context the context
	 * @param entity the entity
	 * @param name the name
	 * @param responseListener the response listener
	 */
    public void writeResponseData(Context context,HttpEntity entity,String name,AbFileHttpResponseListener responseListener){
        
    	if(entity == null){
        	return;
        }
    	
    	if(responseListener.getFile() == null){
    		//??????????????????
    		responseListener.setFile(context,name);
        }
    	
    	InputStream inStream = null;
    	FileOutputStream outStream = null;
        try {
	        inStream = entity.getContent();
	        long contentLength = entity.getContentLength();
	        outStream = new FileOutputStream(responseListener.getFile());
	        if (inStream != null) {
	          
	              byte[] tmp = new byte[4096];
	              int l, count = 0;
	              while ((l = inStream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted()) {
	                  count += l;
	                  outStream.write(tmp, 0, l);
	                  responseListener.sendProgressMessage(count, (int) contentLength);
	              }
	        }
	        responseListener.sendSuccessMessage(200);
	    }catch(Exception e){
	        e.printStackTrace();
	        //??????????????????
			responseListener.sendFailureMessage(AbHttpStatus.RESPONSE_TIMEOUT_CODE,AbAppConfig.SOCKET_TIMEOUT_EXCEPTION,e);
	    } finally {
        	try {
        		if(inStream!=null){
        			inStream.close();
        		}
        		if(outStream!=null){
        			outStream.flush();
    				outStream.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        
    }
    
    /**
     * ??????????????????????????????????????????.
     *
     * @param entity the entity
     * @param responseListener the response listener
     */
    public void readResponseData(HttpEntity entity,AbBinaryHttpResponseListener responseListener){
        
    	if(entity == null){
        	return;
        }
    	
        InputStream inStream = null;
        ByteArrayOutputStream outSteam = null; 
       
    	try {
    		inStream = entity.getContent();
			outSteam = new ByteArrayOutputStream(); 
			long contentLength = entity.getContentLength();
			if (inStream != null) {
				  int l, count = 0;
				  byte[] tmp = new byte[4096];
				  while((l = inStream.read(tmp))!=-1){ 
					  count += l;
			          outSteam.write(tmp,0,l); 
			          responseListener.sendProgressMessage(count, (int) contentLength);

			     } 
			  }
			 responseListener.sendSuccessMessage(HttpStatus.SC_OK,outSteam.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			//??????????????????
			responseListener.sendFailureMessage(AbHttpStatus.RESPONSE_TIMEOUT_CODE,AbAppConfig.SOCKET_TIMEOUT_EXCEPTION,e);
		}finally{
			try {
				if(inStream!=null){
					inStream.close();
				}
				if(outSteam!=null){
					outSteam.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	
        
    }
    
    /**
     * ?????????????????????????????????.
     *
     * @param timeout ??????
     */
    public void setTimeout(int timeout) {
    	this.mTimeout = timeout;
	}

	/**
	 * ?? 2012 amsoft.cn
	 * ?????????ResponderHandler.java 
	 * ?????????????????????
	 *
	 * @author ???????????????
	 * @version v1.0
	 * @date???2013-11-13 ??????3:22:30
	 */
    private static class ResponderHandler extends Handler {
		
		/** ????????????. */
		private Object[] response;
		
		/** ??????????????????. */
		private AbHttpResponseListener responseListener;
		

		/**
		 * ??????????????????.
		 *
		 * @param responseListener the response listener
		 */
		public ResponderHandler(AbHttpResponseListener responseListener) {
			this.responseListener = responseListener;
		}

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case SUCCESS_MESSAGE:
				response = (Object[]) msg.obj;
				
				if (response != null){
					if(responseListener instanceof AbStringHttpResponseListener){
						if(response.length >= 2){
							((AbStringHttpResponseListener)responseListener).onSuccess((Integer) response[0],(String)response[1]);
						}else{
							AbLogUtil.e(mContext, "SUCCESS_MESSAGE "+AbAppConfig.MISSING_PARAMETERS);
						}
						
					}else if(responseListener instanceof AbBinaryHttpResponseListener){
						if(response.length >= 2){ 
						    ((AbBinaryHttpResponseListener)responseListener).onSuccess((Integer) response[0],(byte[])response[1]);
						}else{
							AbLogUtil.e(mContext, "SUCCESS_MESSAGE "+AbAppConfig.MISSING_PARAMETERS);
						}
					}else if(responseListener instanceof AbFileHttpResponseListener){
						
						if(response.length >= 1){ 
							AbFileHttpResponseListener mAbFileHttpResponseListener = ((AbFileHttpResponseListener)responseListener);
							mAbFileHttpResponseListener.onSuccess((Integer) response[0],mAbFileHttpResponseListener.getFile());
						}else{
							AbLogUtil.e(mContext, "SUCCESS_MESSAGE "+AbAppConfig.MISSING_PARAMETERS);
						}
						
					}
				}
                break;
			case FAILURE_MESSAGE:
				 response = (Object[]) msg.obj;
                 if (response != null && response.length >= 3){
                	 //???????????????????????????
                	 AbAppException exception = new AbAppException((Exception) response[2]);
					 responseListener.onFailure((Integer) response[0], (String) response[1], exception);
                 }else{
                	 AbLogUtil.e(mContext, "FAILURE_MESSAGE "+AbAppConfig.MISSING_PARAMETERS);
                 }
	             break;
			case START_MESSAGE:
				responseListener.onStart();
				break;
			case FINISH_MESSAGE:
				responseListener.onFinish();
				break;
			case PROGRESS_MESSAGE:
				 response = (Object[]) msg.obj;
	             if (response != null && response.length >= 2){
	            	 responseListener.onProgress((Long) response[0], (Long) response[1]);
			     }else{
			    	 AbLogUtil.e(mContext, "PROGRESS_MESSAGE "+AbAppConfig.MISSING_PARAMETERS);
			     }
	             break;
			case RETRY_MESSAGE:
				responseListener.onRetry();
				break;
			default:
				break;
		   }
		}
		
	}
    
    /**
     * HTTP????????????
     * @return
     */
    public BasicHttpParams getHttpParams(){
    	
    	BasicHttpParams httpParams = new BasicHttpParams();
        
        // ?????????????????????????????????
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(30);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
		// ???????????????????????????????????????????????????1???
	    ConnManagerParams.setTimeout(httpParams, mTimeout);
	    ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(DEFAULT_MAX_CONNECTIONS));
	    ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);
	    // ??????????????????????????????
	    HttpConnectionParams.setSoTimeout(httpParams, mTimeout);
	    HttpConnectionParams.setConnectionTimeout(httpParams, mTimeout);
	    HttpConnectionParams.setTcpNoDelay(httpParams, true);
	    HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

	    HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setUserAgent(httpParams,userAgent);
	    //????????????
        HttpClientParams.setRedirecting(httpParams, false);
        HttpClientParams.setCookiePolicy(httpParams,
                CookiePolicy.BROWSER_COMPATIBILITY);
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
		return httpParams;
	      
    }
    
    /**
     * ??????HttpClient????????????????????????????????????????????????AuthSSLProtocolSocketFactory???
     * @return
     */
    public DefaultHttpClient getHttpClient(){
    	
    	if(mHttpClient != null){
    		return mHttpClient;
    	}else{
    		return createHttpClient();
    	}
    }
    
    /**
     * ??????HttpClient????????????????????????????????????????????????AuthSSLProtocolSocketFactory???
     * @param httpParams
     * @return
     */
    public DefaultHttpClient createHttpClient(){
    	BasicHttpParams httpParams = getHttpParams();
    	if(mIsOpenEasySSL){
    		 // ??????https???   SSL?????????????????????
    	     EasySSLProtocolSocketFactory easySSLProtocolSocketFactory = new EasySSLProtocolSocketFactory();
             SchemeRegistry supportedSchemes = new SchemeRegistry();
             SocketFactory socketFactory = PlainSocketFactory.getSocketFactory();
             supportedSchemes.register(new Scheme("http", socketFactory, 80));
             supportedSchemes.register(new Scheme("https",easySSLProtocolSocketFactory, 443));
             //?????????ThreadSafeClientConnManager???????????????????????????HttpClient
             ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(
                     httpParams, supportedSchemes);
             //??????HttpClient ThreadSafeClientConnManager
             mHttpClient = new DefaultHttpClient(connectionManager, httpParams);
    	}else{
    		 //???????????????HttpClient
    		 mHttpClient = new DefaultHttpClient(httpParams);
    	}
    	//????????????
    	mHttpClient.setHttpRequestRetryHandler(mRequestRetryHandler);
    	mHttpClient.setCookieStore(mCookieStore);
 	    return mHttpClient;
    }

    /**
     * ????????????ssl ?????????
     */
    public boolean isOpenEasySSL(){
        return mIsOpenEasySSL;
    }


    /**
     * ??????ssl ?????????
     * @param isOpenEasySSL
     */
    public void setOpenEasySSL(boolean isOpenEasySSL){
        this.mIsOpenEasySSL = isOpenEasySSL;
    }
    
    /**
     * ??????ResponseHandler??????????????????,???????????????
     */
    private class RedirectionResponseHandler implements ResponseHandler<String>{
        
    	private AbHttpResponseListener mResponseListener = null;
    	private String mUrl = null;
    	
		public RedirectionResponseHandler(String url,
				AbHttpResponseListener responseListener) {
			super();
			this.mUrl = url;
			this.mResponseListener = responseListener;
		}


		@Override
        public String handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException{
            HttpUriRequest request = (HttpUriRequest) mHttpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
            //????????????  
  			int statusCode = response.getStatusLine().getStatusCode();
  			HttpEntity entity = response.getEntity();
  			String responseBody = null;
            //200??????????????????
            if (statusCode == HttpStatus.SC_OK) {
                
                // ???????????????response body
                // ??????request???abort??????  
                // request.abort();  
                
                if (entity != null){
	  				  if(mResponseListener instanceof AbStringHttpResponseListener){
	  					  //entity??????????????????????????????,??????Content has been consumed
	  					  //?????????????????????
	                      Header header = entity.getContentEncoding();
	                      if (header != null){
	                          String contentEncoding = header.getValue();
	                          if (contentEncoding != null){
	                              if (contentEncoding.contains("gzip")){
	                                  entity = new AbGzipDecompressingEntity(entity);
	                              }
	                          }
	                      }
	                      String charset = EntityUtils.getContentCharSet(entity) == null ? encode : EntityUtils.getContentCharSet(entity);
	      	              responseBody = new String(EntityUtils.toByteArray(entity), charset);
	  					  
	      	              ((AbStringHttpResponseListener)mResponseListener).sendSuccessMessage(statusCode, responseBody);
	  				  }else if(mResponseListener instanceof AbBinaryHttpResponseListener){
	  					  responseBody = "Binary";
	  					  readResponseData(entity,((AbBinaryHttpResponseListener)mResponseListener));
	  				  }else if(mResponseListener instanceof AbFileHttpResponseListener){
	  					  //???????????????
	  					  String fileName = AbFileUtil.getCacheFileNameFromUrl(mUrl, response);
	  					  writeResponseData(mContext,entity,fileName,((AbFileHttpResponseListener)mResponseListener));
	  				  }
	      		      //????????????!!!
	            	  try {
						  entity.consumeContent();
					  } catch (Exception e) {
						  e.printStackTrace();
					  }
	      			  return responseBody;
                }
                
            }
            //301 302?????????????????????
            else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
                    || statusCode == HttpStatus.SC_MOVED_PERMANENTLY){
                // ??????????????????????????????
                Header locationHeader = response.getLastHeader("location");
                String location = locationHeader.getValue();
                if (request.getMethod().equalsIgnoreCase(HTTP_POST)){
                    doPost(location, null, mResponseListener);
                }
                else if (request.getMethod().equalsIgnoreCase(HTTP_GET)){
                    doGet(location, null, mResponseListener);
                }
            }else if(statusCode == HttpStatus.SC_NOT_FOUND){
            	//404
            	mResponseListener.sendFailureMessage(statusCode, AbAppConfig.NOT_FOUND_EXCEPTION, new AbAppException(AbAppConfig.NOT_FOUND_EXCEPTION));
            }else if(statusCode == HttpStatus.SC_FORBIDDEN){
            	//403
            	mResponseListener.sendFailureMessage(statusCode, AbAppConfig.FORBIDDEN_EXCEPTION, new AbAppException(AbAppConfig.FORBIDDEN_EXCEPTION));
            }else{
  				mResponseListener.sendFailureMessage(statusCode, AbAppConfig.REMOTE_SERVICE_EXCEPTION, new AbAppException(AbAppConfig.REMOTE_SERVICE_EXCEPTION));
            }
            return null;
        }
    }
    
    /**
     * ??????????????????
     */
    private HttpRequestRetryHandler mRequestRetryHandler = new HttpRequestRetryHandler(){
        
    	// ????????????????????????
        public boolean retryRequest(IOException exception, int executionCount,
                HttpContext context){
            // ?????????????????????????????????????????????????????????DEFAULT_MAX_RETRIES???
            if (executionCount >= DEFAULT_MAX_RETRIES){
                // ?????????????????????????????????????????????????????????
            	AbLogUtil.d(mContext, "????????????????????????????????????");
                return false;
            }
            if (exception instanceof NoHttpResponseException){
                // ????????????????????????????????????????????????
            	AbLogUtil.d(mContext, "?????????????????????????????????");
                return true;
            }
            if (exception instanceof SSLHandshakeException){
                // SSL????????????????????????
            	AbLogUtil.d(mContext, "ssl ?????? ?????????");
                return false;
            }
            HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
            if (!idempotent){
            	// ???????????????????????????????????????????????????
            	AbLogUtil.d(mContext, "????????????????????????????????????");
                return true;
            }
            if (exception != null){
                return true;
            }
            return false;
        }
    };
    
	private String readString(InputStream is) {
		StringBuffer rst = new StringBuffer();
		
		byte[] buffer = new byte[1048576];
		int len = 0;
		
		try {
			while ((len = is.read(buffer)) > 0)
				for (int i = 0; i < len; ++i)
					rst.append((char)buffer[i]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return rst.toString();
	}


    /**
     * ??????????????????
     * @return
     */
    public String getUserAgent(){
        return userAgent;
    }


    /**
     * ??????????????????
     * @param userAgent
     */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	
	/**
     * ????????????
     * @return
     */
	public String getEncode() {
		return encode;
	}

	/**
	 * ????????????
	 * @param encode
	 */
	public void setEncode(String encode) {
		this.encode = encode;
	}


	/**
	 * ??????HttpClient
	 */
	public void shutdown(){
	    if(mHttpClient != null && mHttpClient.getConnectionManager() != null){
	    	mHttpClient.getConnectionManager().shutdown();
	    }
	}


	public CookieStore getCookieStore() {
		if(mHttpClient!=null){
			mCookieStore = mHttpClient.getCookieStore();
		}
		return mCookieStore;
	}


	public void setCookieStore(CookieStore cookieStore) {
		this.mCookieStore = cookieStore;
	}
	
}
