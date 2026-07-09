package ec.edu.uteq.presustentaciones.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import ec.edu.uteq.presustentaciones.entities.Acta;
import ec.edu.uteq.presustentaciones.entities.ActaFirma;
import ec.edu.uteq.presustentaciones.entities.Evaluacion;
import ec.edu.uteq.presustentaciones.entities.Jurado;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.enums.EstadoSolicitud;
import ec.edu.uteq.presustentaciones.repositories.ActaFirmaRepository;
import ec.edu.uteq.presustentaciones.repositories.ActaRepository;
import ec.edu.uteq.presustentaciones.repositories.EvaluacionRepository;
import ec.edu.uteq.presustentaciones.repositories.JuradoRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActaServiceImpl implements ActaService {

    private final ActaRepository actaRepository;
    private final ActaFirmaRepository actaFirmaRepository;
    private final SolicitudRepository solicitudRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final JuradoRepository juradoRepository;

    @Value("${app.actas.dir:uploads/actas}")
    private String actasDir;

    private static final DeviceRgb UTEQ_GREEN = new DeviceRgb(0, 100, 60);
    private static final DeviceRgb UTEQ_GREEN_LIGHT = new DeviceRgb(220, 245, 230);
    private static final DeviceRgb UTEQ_GREEN_DARK = new DeviceRgb(0, 70, 42);
    private static final DeviceRgb UTEQ_GOLD = new DeviceRgb(184, 143, 0);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb MEDIUM_GRAY = new DeviceRgb(200, 200, 200);

    @Override
    @Transactional
    public Acta generarActa(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));
        Optional<Evaluacion> evalOpt = evaluacionRepository.findBySolicitudId(solicitudId);
        List<Jurado> jurados = juradoRepository.findBySolicitudId(solicitudId);
        try { Path dir = Paths.get(actasDir); if (!Files.exists(dir)) Files.createDirectories(dir); }
        catch (IOException e) { throw new RuntimeException("No se pudo crear directorio de actas: " + e.getMessage()); }

        String nombreArchivo = "acta_" + solicitudId + "_" + System.currentTimeMillis() + ".pdf";
        String rutaCompleta = actasDir + "/" + nombreArchivo;
        generarPdf(rutaCompleta, solicitud, evalOpt.orElse(null), jurados, null);

        Acta acta = actaRepository.findBySolicitudId(solicitudId)
                .orElse(Acta.builder().solicitud(solicitud).fechaGeneracion(LocalDate.now()).build());
        acta.setArchivoPdf(nombreArchivo);
        acta.setFechaGeneracion(LocalDate.now());
        acta = actaRepository.save(acta);
        if (acta.getFirmas() == null || acta.getFirmas().isEmpty()) {
            for (String rol : new String[]{"PRESIDENTE", "VOCAL_1", "VOCAL_2", "TUTOR"}) {
                actaFirmaRepository.save(ActaFirma.builder().acta(acta).rolFirmante(rol).firmada(false).build());
            }
            acta = actaRepository.findById(acta.getId()).orElse(acta);
        }
        return acta;
    }

    @Override @Transactional
    public Acta firmarActa(Long actaId, String rol) {
        Acta acta = actaRepository.findById(actaId).orElseThrow(() -> new RuntimeException("Acta no encontrada: " + actaId));
        String rolUpper = rol.toUpperCase();
        if (!List.of("PRESIDENTE", "VOCAL_1", "VOCAL_2", "TUTOR").contains(rolUpper))
            throw new RuntimeException("Rol invalido: " + rol);
        ActaFirma firma = actaFirmaRepository.findByActaIdAndRolFirmante(actaId, rolUpper)
                .orElseThrow(() -> new RuntimeException("No se encontro firma para rol: " + rolUpper));
        firma.setFirmada(true); firma.setFechaFirma(LocalDateTime.now()); actaFirmaRepository.save(firma);
        acta = actaRepository.findById(actaId).orElse(acta);
        if (acta.isFirmada()) {
            Solicitud solicitud = acta.getSolicitud();
            solicitud.setEstado(EstadoSolicitud.COMPLETADA); solicitudRepository.save(solicitud);
            if (acta.getArchivoPdf() != null) {
                Optional<Evaluacion> evalOpt = evaluacionRepository.findBySolicitudId(solicitud.getId());
                List<Jurado> jurados = juradoRepository.findBySolicitudId(solicitud.getId());
                generarPdf(actasDir + "/" + acta.getArchivoPdf(), solicitud, evalOpt.orElse(null), jurados, acta);
            }
        }
        return acta;
    }

    @Override public byte[] obtenerPdfBytes(Long actaId) {
        Acta acta = actaRepository.findById(actaId).orElseThrow(() -> new RuntimeException("Acta no encontrada"));
        if (acta.getArchivoPdf() == null) throw new RuntimeException("El acta no tiene PDF generado.");
        try { return Files.readAllBytes(Paths.get(actasDir, acta.getArchivoPdf())); }
        catch (IOException e) { throw new RuntimeException("No se pudo leer el PDF: " + e.getMessage()); }
    }

    @Override public String obtenerNombreArchivo(Long actaId) {
        Acta acta = actaRepository.findById(actaId).orElseThrow(() -> new RuntimeException("Acta no encontrada"));
        Solicitud s = acta.getSolicitud();
        String nom = "estudiante"; String car = "";
        if (s != null && s.getEstudiante() != null && s.getEstudiante().getUsuario() != null) {
            nom = s.getEstudiante().getUsuario().getNombre() + "_" + s.getEstudiante().getUsuario().getApellido();
            car = s.getEstudiante().getCarrera() != null ? "_" + s.getEstudiante().getCarrera().replace(" ", "_") : "";
        }
        return ("Acta_PreSustentacion_" + nom + car + ".pdf").replace(" ", "_");
    }

    @Override public List<Acta> listarActas() { return actaRepository.findAll(); }
    @Override public Optional<Acta> buscarPorSolicitud(Long solicitudId) { return actaRepository.findBySolicitudId(solicitudId); }

    // ══════════════════════════════════════════════════════════════════════════
    // PDF GENERATION - MODERN GREEN THEME
    // ══════════════════════════════════════════════════════════════════════════

    private void generarPdf(String ruta, Solicitud solicitud, Evaluacion evaluacion, List<Jurado> jurados, Acta acta) {
        try {
            PdfFont fR = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fB = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfWriter writer = new PdfWriter(ruta);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);
            doc.setMargins(30, 40, 30, 40);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // HEADER
            Table h = new Table(UnitValue.createPercentArray(new float[]{22f,56f,22f})).setWidth(UnitValue.createPercentValue(100));
            h.addCell(new Cell().add(new Paragraph("UTEQ").setFont(fB).setFontSize(20).setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(UTEQ_GREEN).setBorder(Border.NO_BORDER).setPadding(14));
            h.addCell(new Cell().add(new Paragraph("ACTA DE PRE-SUSTENTACION").setFont(fB).setFontSize(13).setFontColor(UTEQ_GREEN).setTextAlignment(TextAlignment.CENTER)).add(new Paragraph("Universidad Tecnica Estatal de Quevedo").setFont(fR).setFontSize(8).setFontColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER)).add(new Paragraph("Facultad de Ciencias de la Computacion y Diseno Digital").setFont(fR).setFontSize(7).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER).setPadding(10));
            h.addCell(new Cell().add(new Paragraph("Acta No. " + solicitud.getId()).setFont(fB).setFontSize(11).setFontColor(UTEQ_GREEN).setTextAlignment(TextAlignment.CENTER)).add(new Paragraph(LocalDate.now().format(fmt)).setFont(fR).setFontSize(9).setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(UTEQ_GREEN_LIGHT).setBorder(Border.NO_BORDER).setPadding(12));
            doc.add(h);
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(2.5f)).setStrokeColor(UTEQ_GREEN));
            doc.add(new Paragraph(" ").setFontSize(4));

            // 1. DATOS DEL ESTUDIANTE
            doc.add(secTitle("1. DATOS DEL ESTUDIANTE", fB));
            String nomEst = solicitud.getEstudiante() != null && solicitud.getEstudiante().getUsuario() != null ? solicitud.getEstudiante().getUsuario().getNombre() + " " + solicitud.getEstudiante().getUsuario().getApellido() : "-";
            String carrera = solicitud.getEstudiante() != null && solicitud.getEstudiante().getCarrera() != null ? solicitud.getEstudiante().getCarrera() : "-";
            Table d = new Table(UnitValue.createPercentArray(new float[]{28f,72f})).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
            addR(d, "Estudiante:", nomEst, fB, fR); addR(d, "Carrera:", carrera, fB, fR);
            addR(d, "Titulo del tema:", solicitud.getTituloTema() != null ? solicitud.getTituloTema() : "-", fB, fR);
            addR(d, "Modalidad:", fmtMod(solicitud.getModalidad()), fB, fR);
            addR(d, "Fecha de solicitud:", solicitud.getFechaRegistro() != null ? solicitud.getFechaRegistro().format(fmtDt) : "-", fB, fR);
            doc.add(d);

            // 2. TRIBUNAL EVALUADOR
            doc.add(secTitle("2. TRIBUNAL EVALUADOR", fB));
            Table t = new Table(UnitValue.createPercentArray(new float[]{8f,38f,27f,27f})).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
            for (String hdr : new String[]{"#","Docente","Rol","Estado"})
                t.addCell(new Cell().add(new Paragraph(hdr).setFont(fB).setFontSize(8).setFontColor(ColorConstants.WHITE)).setBackgroundColor(UTEQ_GREEN).setPadding(7).setBorder(Border.NO_BORDER));
            if (jurados.isEmpty()) { t.addCell(new Cell(1,4).add(new Paragraph("Sin jurados asignados").setFont(fR).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(10).setBackgroundColor(LIGHT_GRAY)); }
            else { int idx=1; for (Jurado j : jurados) {
                String dn = j.getDocente()!=null&&j.getDocente().getUsuario()!=null ? j.getDocente().getUsuario().getNombre()+" "+j.getDocente().getUsuario().getApellido() : "-";
                DeviceRgb bg = idx%2==0 ? LIGHT_GRAY : new DeviceRgb(255,255,255);
                t.addCell(dc(String.valueOf(idx),fR,bg)); t.addCell(dc(dn,fB,bg)); t.addCell(dc(fmtRol(j.getRol()),fR,bg)); t.addCell(dc("Asignado",fR,bg)); idx++;
            }}
            doc.add(t);

            // 3. EVALUACION Y CALIFICACION
            doc.add(secTitle("3. EVALUACION Y CALIFICACION", fB));
            if (evaluacion != null) {
                double nI = evaluacion.getNotaInstructor()!=null?evaluacion.getNotaInstructor():0;
                double nJ = evaluacion.getNotaJurado()!=null?evaluacion.getNotaJurado():0;
                double nF = (nI*0.6)+(nJ*0.4); String res = nF>=7?"APROBADO":"REPROBADO";
                Table ev = new Table(UnitValue.createPercentArray(new float[]{45f,18f,18f,19f})).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(8);
                for (String hdr : new String[]{"Concepto","Peso","Nota","Ponderado"})
                    ev.addCell(new Cell().add(new Paragraph(hdr).setFont(fB).setFontSize(8).setFontColor(ColorConstants.WHITE)).setBackgroundColor(UTEQ_GREEN).setPadding(7).setBorder(Border.NO_BORDER));
                DeviceRgb w = new DeviceRgb(255,255,255);
                ev.addCell(dc("Nota del Instructor (Titulacion II)",fR,w)); ev.addCell(dc("60%",fR,w)); ev.addCell(dc(String.format("%.2f",nI),fB,w)); ev.addCell(dc(String.format("%.2f",nI*0.6),fR,w));
                ev.addCell(dc("Nota del Tribunal (Promedio jurados)",fR,LIGHT_GRAY)); ev.addCell(dc("40%",fR,LIGHT_GRAY)); ev.addCell(dc(String.format("%.2f",nJ),fB,LIGHT_GRAY)); ev.addCell(dc(String.format("%.2f",nJ*0.4),fR,LIGHT_GRAY));
                doc.add(ev);
                // Nota final
                Table nft = new Table(UnitValue.createPercentArray(new float[]{55f,25f,20f})).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(10);
                nft.addCell(new Cell().add(new Paragraph("NOTA FINAL").setFont(fB).setFontSize(11).setFontColor(ColorConstants.WHITE)).setBackgroundColor(UTEQ_GREEN_DARK).setBorder(Border.NO_BORDER).setPadding(10));
                nft.addCell(new Cell().add(new Paragraph(String.format("%.2f / 10",nF)).setFont(fB).setFontSize(13).setFontColor(ColorConstants.WHITE)).setBackgroundColor(UTEQ_GREEN_DARK).setBorder(Border.NO_BORDER).setPadding(10).setTextAlignment(TextAlignment.CENTER));
                DeviceRgb rb = nF>=7 ? new DeviceRgb(22,163,74) : new DeviceRgb(220,38,38);
                nft.addCell(new Cell().add(new Paragraph(res).setFont(fB).setFontSize(10).setFontColor(ColorConstants.WHITE)).setBackgroundColor(rb).setBorder(Border.NO_BORDER).setPadding(10).setTextAlignment(TextAlignment.CENTER));
                doc.add(nft);
                if (evaluacion.getObservaciones()!=null&&!evaluacion.getObservaciones().isBlank()) {
                    doc.add(new Paragraph("Observaciones del coordinador:").setFont(fB).setFontSize(9).setFontColor(UTEQ_GREEN).setMarginBottom(4));
                    doc.add(new Paragraph(evaluacion.getObservaciones()).setFont(fR).setFontSize(9).setBackgroundColor(UTEQ_GREEN_LIGHT).setPadding(10).setBorderLeft(new SolidBorder(UTEQ_GREEN,3)).setMarginBottom(12));
                }
            } else { doc.add(new Paragraph("Evaluacion pendiente de registro.").setFont(fR).setFontSize(9).setFontColor(ColorConstants.GRAY).setBackgroundColor(LIGHT_GRAY).setPadding(10).setMarginBottom(12)); }

            // 4. FIRMAS ELECTRONICAS
            doc.add(secTitle("4. FIRMAS ELECTRONICAS", fB));
            Table ft = new Table(UnitValue.createPercentArray(new float[]{25f,25f,25f,25f})).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
            String[] rl = {"Presidente","Vocal 1","Vocal 2","Tutor"};
            String[] rc = {"PRESIDENTE","VOCAL_1","VOCAL_2","TUTOR"};
            for (int i=0;i<4;i++) {
                boolean firmado=false; LocalDateTime ff=null;
                if (acta!=null&&acta.getFirmas()!=null) { final String rb2=rc[i]; Optional<ActaFirma> fo=acta.getFirmas().stream().filter(f->f.getRolFirmante().equals(rb2)).findFirst(); if(fo.isPresent()){firmado=fo.get().isFirmada();ff=fo.get().getFechaFirma();}}
                DeviceRgb fbg=firmado?UTEQ_GREEN_LIGHT:LIGHT_GRAY; DeviceRgb fbc=firmado?UTEQ_GREEN:MEDIUM_GRAY; DeviceRgb fc=firmado?UTEQ_GREEN:new DeviceRgb(120,120,120);
                Cell c2=new Cell().add(new Paragraph(rl[i]).setFont(fB).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setFontColor(UTEQ_GREEN_DARK)).add(new Paragraph(firmado?"FIRMADO":"PENDIENTE").setFont(fB).setFontSize(9).setFontColor(fc).setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(fbg).setBorder(new SolidBorder(fbc,1)).setPadding(10).setMargin(2);
                if(firmado&&ff!=null) c2.add(new Paragraph(ff.format(fmtDt)).setFont(fR).setFontSize(6).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
                ft.addCell(c2);
            }
            doc.add(ft);

            // FOOTER
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)).setStrokeColor(UTEQ_GREEN));
            doc.add(new Paragraph("Documento generado el " + LocalDateTime.now().format(fmtDt) + " | Sistema de Gestion de Pre-Sustentaciones UTEQ | Documento oficial").setFont(fR).setFontSize(7).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(4));
            doc.close();
        } catch (IOException e) { throw new RuntimeException("Error generando PDF: " + e.getMessage(), e); }
    }

    // HELPERS
    private Paragraph secTitle(String text, PdfFont f) { return new Paragraph(text).setFont(f).setFontSize(10).setFontColor(UTEQ_GREEN).setBorderBottom(new SolidBorder(UTEQ_GOLD,1.5f)).setMarginTop(8).setMarginBottom(6); }
    private void addR(Table t, String l, String v, PdfFont fb, PdfFont fr) { t.addCell(new Cell().add(new Paragraph(l).setFont(fb).setFontSize(9).setFontColor(UTEQ_GREEN_DARK)).setBackgroundColor(UTEQ_GREEN_LIGHT).setBorder(Border.NO_BORDER).setPadding(6)); t.addCell(new Cell().add(new Paragraph(v).setFont(fr).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setBorderBottom(new SolidBorder(new DeviceRgb(230,230,230),0.5f))); }
    private Cell dc(String text, PdfFont f, DeviceRgb bg) { return new Cell().add(new Paragraph(text).setFont(f).setFontSize(9)).setPadding(6).setBackgroundColor(bg).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(new DeviceRgb(220,220,220),0.5f)); }
    private String fmtRol(String r) { if(r==null)return"-"; switch(r){case"PRESIDENTE":return"Presidente";case"VOCAL_1":return"Vocal 1";case"VOCAL_2":return"Vocal 2";case"TUTOR":return"Tutor";default:return r;} }
    private String fmtMod(String m) { if(m==null)return"-"; String r=m.replace("_"," "); String[] p=r.toLowerCase().split(" "); StringBuilder sb=new StringBuilder(); for(String w:p){if(sb.length()>0)sb.append(" ");if(w.length()>0)sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));} return sb.toString(); }
}
