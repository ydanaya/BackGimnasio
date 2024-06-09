package co.edu.ufps.gimnasio.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.ufps.gimnasio.model.dto.RutinaDTO;
import co.edu.ufps.gimnasio.model.entity.Ejercicio;
import co.edu.ufps.gimnasio.model.entity.EjercicioEquipamiento;
import co.edu.ufps.gimnasio.model.entity.Equipamiento;
import co.edu.ufps.gimnasio.model.entity.Rutina;
import co.edu.ufps.gimnasio.model.entity.RutinaEjercicio;
import co.edu.ufps.gimnasio.model.entity.UsuarioRutina;
import co.edu.ufps.gimnasio.repository.RutinaEjercicioRepository;
import co.edu.ufps.gimnasio.repository.RutinaRepository;
import co.edu.ufps.gimnasio.repository.UsuarioRutinaRepository;
import co.edu.ufps.gimnasio.service.RutinaService;

@Service

public class RutinaServiceImpl implements RutinaService {

	@Autowired

	RutinaRepository rutinaRepository;
	@Autowired
	RutinaEjercicioRepository rutinaEjercicioRepository;
	@Autowired
	UsuarioRutinaRepository usuarioRutinaRepository;

	@Override
	public List<Rutina> finAllRutinas() {
		return rutinaRepository.findByEstado(true);
	}

	@Override
	public Rutina findById(Integer id) {
		Optional<Rutina> rutina = rutinaRepository.findById(id);
		if (rutina.isPresent()) {
			return rutina.get();
		}
		return null;
	}

	@Override
	public Rutina saveRutina(RutinaDTO rutina) {
		ModelMapper modelMapper = new ModelMapper();
		Rutina rutinaCurrent = modelMapper.map(rutina, Rutina.class);
		rutinaCurrent.setEjercicios(null);
		rutinaCurrent.setNombre(rutinaCurrent.getNombre().toUpperCase());
		rutinaCurrent.setEstado(true);
		Rutina rutinaReturn = rutinaRepository.save(rutinaCurrent);

		List<Ejercicio> ejercicios = rutina.getEjercicios();
		for (Ejercicio ejercicio : ejercicios) {

			RutinaEjercicio rutinaEjercicio = new RutinaEjercicio();
			rutinaEjercicio.setEjercicioId(ejercicio.getId());
			rutinaEjercicio.setRutinaId(rutinaReturn.getId());
			rutinaEjercicioRepository.save(rutinaEjercicio);

		}

		return rutinaReturn;
	}

	@Override
	public Rutina updateRutina(RutinaDTO rutina) {
		Optional<Rutina> rutinaCurrent = rutinaRepository.findById(rutina.getId());
		if (rutinaCurrent.isPresent()) {
			Rutina rutinaReturn = rutinaCurrent.get();
			rutinaReturn.setNombre(rutina.getNombre());
			rutinaReturn.setDescripcion(rutina.getDescripcion());
			rutinaReturn.setDuracion(rutina.getDuracion());
			rutinaReturn.setMusculo(rutina.getMusculo());
			List<Ejercicio> ejercicios = rutina.getEjercicios();

			List<RutinaEjercicio> ejerciciosOld = rutinaEjercicioRepository.findByRutinaId(rutina.getId());

			// Crear una lista de IDs de los nuevos equipamientos
			List<Integer> nuevosEjerciciosIds = ejercicios.stream().map(Ejercicio::getId).collect(Collectors.toList());
			// Crear una lista de IDs de los equipamientos antiguos
			List<Integer> ejerciciosOldIds = ejerciciosOld.stream().map(RutinaEjercicio::getEjercicioId)
					.collect(Collectors.toList());

			List<Integer> ejerciciosNuevos = nuevosEjerciciosIds.stream().filter(id -> !ejerciciosOldIds.contains(id))
					.collect(Collectors.toList());

			// Identificar equipamientos a eliminar (antiguos)
			List<Integer> ejerciciosEliminar = ejerciciosOldIds.stream().filter(id -> !nuevosEjerciciosIds.contains(id))
					.collect(Collectors.toList());

			// Eliminar equipamientos antiguos
			for (Integer integer : ejerciciosEliminar) {

				rutinaEjercicioRepository.deleteByRutinaIdAndEjercicioId(rutina.getId(), integer);
			}

			for (Integer integer : ejerciciosNuevos) {
				RutinaEjercicio ejercicio = new RutinaEjercicio();
				ejercicio.setEjercicioId(integer);
				ejercicio.setRutinaId(rutina.getId());
				rutinaEjercicioRepository.save(ejercicio);
			}
			return rutinaRepository.save(rutinaReturn);

		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rutina deleteRutina(Integer id) {

		Optional<Rutina> rutina = rutinaRepository.findById(id);
		if (rutina.isPresent()) {

				eliminarUsuarios(rutina.get()); 
				Rutina rutinaReturn=rutina.get();
				rutinaReturn.setNombre(rutinaReturn.getNombre()+"-"+rutinaReturn.getId());
				rutinaReturn.setEstado(false);
				rutinaRepository.save(rutinaReturn);
				return rutina.get();
		

		}
		return null;
	}

	

	public Boolean eliminarUsuarios(Rutina rutina) {
		List<UsuarioRutina> usuarios = usuarioRutinaRepository.findByRutinaId(rutina);

		if (!usuarios.isEmpty()) {

			usuarioRutinaRepository.deleteAll(usuarios);
		}
		return true;
	}

}
