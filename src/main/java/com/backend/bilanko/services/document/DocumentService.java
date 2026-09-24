package com.backend.bilanko.services.document;

import com.backend.bilanko.DTO.object.document.*;
import com.backend.bilanko.models.person.user.User;

import java.util.List;

public interface DocumentService {

    DocumentBootstrapDTO getBootstrap(User currentUser, int dureeHistorique);

    List<LigneHistoriqueDTO> getHistorique(User currentUser, int dureeHistorique);

    DocumentResponseDTO createPret(User currentUser, CreateDocumentPretRequest request);

    DocumentResponseDTO createFiscal(User currentUser, CreateDocumentFiscalRequest request);

    List<DocumentResponseDTO> listMine(User currentUser);

    DocumentResponseDTO getById(User currentUser, long id);
}
