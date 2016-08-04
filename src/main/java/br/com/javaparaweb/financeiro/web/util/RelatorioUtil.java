package br.com.javaparaweb.financeiro.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.javaparaweb.financeiro.util.UtilException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

public class RelatorioUtil {

	public static final int RELATORIO_PDF = 1;
	public static final int RELATORIO_EXCEL = 2;
	public static final int RELATORIO_HTML = 3;
	public static final int RELATORIO_PLANILHA_OPEN_OFFICE = 4;

	public StreamedContent exportarRelatorio(HashMap<String, Object> parametrosRelatorio, String nomeRelatorioJasper,
			String nomeRelatorioSaida, int tipoRelatorio) throws UtilException {

		StreamedContent arquivoRetorno = null;
		try {
			Connection conexao = this.getConexao();
			FacesContext contextoFaces = FacesContext.getCurrentInstance();
			ExternalContext contextoExterno = contextoFaces.getExternalContext();
			ServletContext contextoServlet = (ServletContext) contextoExterno.getContext();

			String caminhoRelatorios = contextoServlet.getRealPath("/relatorios");
			String caminhoArquivoJasper = caminhoRelatorios + File.separator + nomeRelatorioJasper + ".jasper";
			String caminhoArquivoRelatorio = caminhoRelatorios + File.separator + nomeRelatorioSaida;

			JasperReport relatorioJasper = (JasperReport) JRLoader.loadObjectFromFile(caminhoArquivoJasper);
			JasperPrint impressoraJasper = JasperFillManager.fillReport(relatorioJasper, parametrosRelatorio, conexao);

			String extensao = null;
			File arquivoGerado = null;

			switch (tipoRelatorio) {
			case RelatorioUtil.RELATORIO_PDF:
				JRPdfExporter pdfExportado = new JRPdfExporter();
				extensao = "pdf";
				arquivoGerado = new File(caminhoArquivoRelatorio + "." + extensao);
				pdfExportado.setExporterInput(new SimpleExporterInput(impressoraJasper));
				pdfExportado.setExporterOutput(new SimpleOutputStreamExporterOutput(arquivoGerado));
				pdfExportado.exportReport();
				arquivoGerado.deleteOnExit();
				break;
			case RelatorioUtil.RELATORIO_HTML:
				HtmlExporter htmlExportado = new HtmlExporter();
				extensao = "html";
				arquivoGerado = new File(caminhoArquivoRelatorio + "." + extensao);
				htmlExportado.setExporterInput(new SimpleExporterInput(impressoraJasper));
				htmlExportado.setExporterOutput(new SimpleHtmlExporterOutput(arquivoGerado));
				htmlExportado.exportReport();
				arquivoGerado.deleteOnExit();
				break;
			case RelatorioUtil.RELATORIO_EXCEL:
				JRXlsExporter xlsExportado = new JRXlsExporter();
				extensao = "xls";
				arquivoGerado = new File(caminhoArquivoRelatorio + "." + extensao);
				xlsExportado.setExporterInput(new SimpleExporterInput(impressoraJasper));
				xlsExportado.setExporterOutput(new SimpleOutputStreamExporterOutput(arquivoGerado));
				xlsExportado.exportReport();
				arquivoGerado.deleteOnExit();
				break;
			case RelatorioUtil.RELATORIO_PLANILHA_OPEN_OFFICE:
				JROdtExporter openExportado = new JROdtExporter();
				extensao = "ods";
				arquivoGerado = new File(caminhoArquivoRelatorio + "." + extensao);
				openExportado.setExporterInput(new SimpleExporterInput(impressoraJasper));
				openExportado.setExporterOutput(new SimpleOutputStreamExporterOutput(arquivoGerado));
				openExportado.exportReport();
				arquivoGerado.deleteOnExit();
				break;
			}

			InputStream conteudoRelatorio = new FileInputStream(arquivoGerado);
			arquivoRetorno = new DefaultStreamedContent(conteudoRelatorio, "application/" + extensao,
					nomeRelatorioSaida + "." + extensao);

		} catch (JRException e) {
			System.out.println(e.getMessage());
			throw new UtilException("Não foi possível gerar o relatório.", e);
		} catch (FileNotFoundException e) {
			throw new UtilException("Arquivo do relatório não encontrado.", e);
		}

		return arquivoRetorno;
	}

	public void executarRelatorio(HashMap<String, Object> parametrosRelatorio, String nomeRelatorioJasper,
			String nomeRelatorioSaida) throws UtilException {
		try {
			Connection conexao = this.getConexao();
			FacesContext contextoFaces = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) contextoFaces.getExternalContext().getResponse();

			ExternalContext contextoExterno = contextoFaces.getExternalContext();
			ServletContext contextoServlet = (ServletContext) contextoExterno.getContext();

			String caminhoRelatorios = contextoServlet.getRealPath("/relatorios");
			String caminhoArquivoJasper = caminhoRelatorios + File.separator + nomeRelatorioJasper + ".jasper";
			ServletOutputStream servletOutputStream = response.getOutputStream();

			//@SuppressWarnings({ "rawtypes", "unchecked" })
			JasperPrint jasperPrint = JasperFillManager.fillReport(caminhoArquivoJasper, parametrosRelatorio, conexao);
			byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
			response.setHeader("Content-Disposition","inline; filename="+ nomeRelatorioSaida +".pdf");
            response.setContentType("application/pdf");
			response.setContentLength(bytes.length);
			response.getOutputStream().write(bytes, 0, bytes.length);
			response.getOutputStream().flush();
			response.getOutputStream().close();
			contextoFaces.responseComplete();
			JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
			JasperExportManager.exportReportToPdfFile(jasperPrint, nomeRelatorioSaida+".pdf");
			contextoFaces.responseComplete();
		} catch(Exception e) {
			throw new UtilException("Não foi possível gerar o relatório em tela.", e);
		}

	}

	private Connection getConexao() throws UtilException {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env/");
			DataSource ds = (DataSource) envContext.lookup("jdbc/FinanceiroDB");
			return (Connection) ds.getConnection();
		} catch (NamingException e) {
			throw new UtilException("Não foi possível encontrar o nome da conexão do banco.", e);
		} catch (SQLException e) {
			throw new UtilException("Ocorreu um erro de SQL.", e);
		}
	}

}
