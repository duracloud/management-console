package org.duraspace.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class SimpleExceptionResolver extends SimpleMappingExceptionResolver
{
	public ModelAndView resolveException(HttpServletRequest request,
	                                     HttpServletResponse response,
	                                     Object handler,
	                                     Exception ex)
	{
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage());

        StringWriter stackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(stackTrace));
        mav.addObject("stack", stackTrace.toString());

		return mav;
	}
}
