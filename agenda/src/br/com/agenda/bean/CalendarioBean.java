package br.com.agenda.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

//import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import br.com.agenda.dao.EventoBD;
import br.com.agenda.enums.TipoEvento;
import br.com.agenda.modelo.CustomScheduleEvent;
import br.com.agenda.modelo.Evento;
import br.com.agenda.modelo.Usuario;
import br.com.agenda.tx.Transacional;

@Named
@ViewScoped
public class CalendarioBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private ScheduleModel model;
	private Evento evento;
	private List<Evento> eventos;
	private ScheduleEvent event;
	private List<ScheduleEvent> scheduleEvents;

	@Inject
	private EventoBD eventoBD;

	@Inject
	private FacesContext context;

	private static final Logger LOG = Logger.getLogger(CalendarioBean.class.getName());

	public CalendarioBean() {
		event = new CustomScheduleEvent();
		model = new DefaultScheduleModel();
		evento = new Evento();
	}

	@PostConstruct
	public void init() {
		if (this.model != null) {
			eventos = this.eventoBD.listaEventosUser(relacionaUser());
			if (this.scheduleEvents == null) {
				this.scheduleEvents = new ArrayList<ScheduleEvent>();
			}
			carregarEventos();
		}
	}

	public void carregarEventos() {

		if (eventos != null) {
			for (Evento eventoAtual : eventos) {
				ScheduleEvent newEvent = new CustomScheduleEvent(eventoAtual.getTitulo(), eventoAtual.getDescricao(), eventoAtual.getDataInicio(),
						eventoAtual.getDataFim(), eventoAtual.getTipoEvento().getCss(), eventoAtual.isDiaInteiro(),
						eventoAtual);
				if (!this.scheduleEvents.contains(newEvent)) {
					newEvent.setId(eventoAtual.getId().toString());
					this.scheduleEvents.add(newEvent);
					this.model.addEvent(newEvent);
				}
			}
		}

	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Usuario relacionaUser() {
		Usuario usuario = (Usuario) context.getExternalContext().getSessionMap().get("usuarioLogado");
		return usuario;
	}

	@Transacional
	public void salvar() {
		evento.setUsuario(relacionaUser());
		ScheduleEvent newEvent = new CustomScheduleEvent(evento.getTitulo(), evento.getDescricao(), evento.getDataInicio(),
				evento.getDataFim(), evento.getTipoEvento().getCss(), evento.isDiaInteiro(), evento);
		if (evento.getId() == null) {
			model.addEvent(newEvent);
			eventoBD.adiciona(evento);
			System.out.println("Evento ID: " + evento.getId().toString());
		} else {
			if (event.getId() == null) {
				System.out.println("ID-NULO");
			}
			newEvent.setId(event.getId());
			model.updateEvent(newEvent);
			eventoBD.atualiza(evento);

		}
		this.setEventos(eventoBD.listaEventosUser(relacionaUser()));
		model.clear();
		carregarEventos();
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Evento Salvo", "Evento Salvo");
		addMessage(message);
	}

	@Transacional
	public void remover() {
		model.deleteEvent(event);
		eventoBD.remove(evento);
		this.setEventos(eventoBD.listaEventosUser(relacionaUser()));
		model.clear();
		carregarEventos();
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Evento Removido", "Evento Removido");
		addMessage(message);
	}

	public void onDateSelect(SelectEvent selectEvent) {
		this.evento = new Evento();
		Date dataSelecionada = (Date) selectEvent.getObject();
		this.evento.setDataInicio(dataSelecionada);
		this.evento.setDataFim(dataSelecionada);
	}

	public void onEventSelect(SelectEvent selectEvent) {
		event = (CustomScheduleEvent) selectEvent.getObject();
		this.evento = (Evento) event.getData();
	}

//    public void onEventResize(ScheduleEntryResizeEvent event) {
//        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Evento Redimensionado", "Dia:" + event.getDayDelta() + ", Hor�rio:" + event.getMinuteDelta());
//        addMessage(message);
//    }

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public TipoEvento[] getTiposEventos() {
		return TipoEvento.values();
	}

	public ScheduleModel getModel() {
		return model;
	}

	public void setModel(ScheduleModel model) {
		this.model = model;
	}

	public static Logger getLog() {
		return LOG;
	}

	public List<ScheduleEvent> getScheduleEvents() {
		return scheduleEvents;
	}

	public void setScheduleEvents(List<ScheduleEvent> scheduleEvents) {
		this.scheduleEvents = scheduleEvents;
	}

	public List<Evento> getEventos() {
		return eventos;
	}

	public void setEventos(List<Evento> eventos) {
		this.eventos = eventos;
	}
}