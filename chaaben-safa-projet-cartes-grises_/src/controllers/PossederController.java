package controllers;

import database.DatabaseConnection;
import models.Posseder;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour gérer les propriétés (relation entre propriétaires et véhicules) dans la base de données.
 */
public class PossederController {

        /**
     * Récupère toutes les relations propriétaire-véhicule avec jointures.
     * @return Liste des objets Posseder contenant les relations propriétaires-véhicules.
     */
    public List<Posseder> getAllPossessions() {
        List<Posseder> possessions = new ArrayList<>();
        String query = "SELECT p.id_proprietaire, p.prenom, p.nom, v.id_vehicule, v.matricule, pos.date_debut_propriete, pos.date_fin_propriete " +
                       "FROM proprietaire p " +
                       "JOIN posseder pos ON p.id_proprietaire = pos.id_proprietaire " +
                       "JOIN vehicule v ON pos.id_vehicule = v.id_vehicule";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Posseder possession = new Posseder(
                        rs.getInt("id_proprietaire"),
                        rs.getInt("id_vehicule"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("matricule"),
                        rs.getDate("date_debut_propriete"),
                        rs.getDate("date_fin_propriete"));
                possessions.add(possession);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return possessions;

    }

        /**
     * Affiche une boîte de dialogue avec un message.
     * @param title Titre de la boîte de dialogue.
     * @param message Message à afficher.
     */
    private void showAlert(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
        /**
     * Ajoute une relation entre un propriétaire et un véhicule.
     * @param nomProprietaire Nom du propriétaire.
     * @param prenomProprietaire Prénom du propriétaire.
     * @param matriculeVehicule Matricule du véhicule.
     * @param dateDebut Date de début de propriété.
     */
    public void addPossession(String nomProprietaire, String prenomProprietaire, String matriculeVehicule, Date dateDebut) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (dateDebut == null) {
                showAlert("Erreur", "La date de début est obligatoire.");
                return;
            }
            // Récupérer l'ID du propriétaire
            int idProprietaire = getProprietaireId(conn, nomProprietaire, prenomProprietaire);
            if (idProprietaire == -1) {
                showAlert("Erreur", "Le propriétaire n'existe pas.");
                return;
            }
            // Récupérer l'ID du véhicule
            int idVehicule = getVehiculeId(conn, matriculeVehicule);
            if (idVehicule == -1) {
                showAlert("Erreur", "Le véhicule n'existe pas.");
                return;
            }
            // Vérifier si la relation existe déjà
            if (existsPossession(idProprietaire, idVehicule)) {
                showAlert("Erreur", "Cette relation existe déjà.");
                return;
            }
            // Insérer la possession
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO POSSEDER (id_proprietaire, id_vehicule, date_debut_propriete) VALUES (?, ?, ?)")) {
                ps.setInt(1, idProprietaire);
                ps.setInt(2, idVehicule);
                ps.setDate(3, dateDebut);
    
                int rowsInserted = ps.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert("Succès", "Possession ajoutée avec succès !");
                } else {
                    showAlert("Erreur", "Erreur lors de l'ajout.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de l'ajout de la possession. Détails : " + e.getMessage());
        }
    }
    
    /**
     * Récupère l'ID d'un propriétaire à partir de son nom et prénom.
     * @param conn Connexion à la base de données.
     * @param nom Nom du propriétaire.
     * @param prenom Prénom du propriétaire.
     * @return ID du propriétaire ou -1 si non trouvé.
     */
    private int getProprietaireId(Connection conn, String nom, String prenom) throws SQLException {
        String query = "SELECT id_proprietaire FROM PROPRIETAIRE WHERE nom = ? AND prenom = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_proprietaire");
                }
            }
        }
        return -1; // Retourne -1 si aucun propriétaire trouvé
    }

        /**
     * Récupère l'ID d'un véhicule à partir de son matricule.
     * @param conn Connexion à la base de données.
     * @param matricule Matricule du véhicule.
     * @return ID du véhicule ou -1 si non trouvé.
     */

    private int getVehiculeId(Connection conn, String matricule) throws SQLException {
        String query = "SELECT id_vehicule FROM VEHICULE WHERE matricule = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, matricule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vehicule");
                }
            }
        }
        return -1; // Retourne -1 si aucun véhicule trouvé
    }

        /**
     * Vérifie si une relation propriétaire-véhicule existe déjà.
     * @param idProprietaire ID du propriétaire.
     * @param idVehicule ID du véhicule.
     * @return true si la relation existe, sinon false.
     */
    private boolean existsPossession(int idProprietaire, int idVehicule) throws SQLException {
        String query = "SELECT COUNT(*) FROM POSSEDER WHERE id_proprietaire = ? AND id_vehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Met à jour la relation entre un propriétaire et un véhicule ainsi que leurs informations.
     *
     * @param idProprietaire       L'identifiant du propriétaire
     * @param idVehicule           L'identifiant du véhicule
     * @param newNomProprietaire   Nouveau nom du propriétaire
     * @param newPrenomProprietaire Nouveau prénom du propriétaire
     * @param newMatriculeVehicule Nouveau matricule du véhicule
     * @param newDateDebut         Nouvelle date de début de possession
     * @param newDateFin           Nouvelle date de fin de possession
     */
    public void updatePossession(int idProprietaire, int idVehicule, String newNomProprietaire, String newPrenomProprietaire, String newMatriculeVehicule, Date newDateDebut, Date newDateFin) {
        // Vérification si la relation existe déjà
        String checkExistingQuery = "SELECT COUNT(*) FROM posseder WHERE id_proprietaire = ? AND id_vehicule = ?";
        // Requête pour mettre à jour les informations du propriétaire
        String updateProprietaireQuery = "UPDATE proprietaires SET nom = ?, prenom = ? WHERE id_proprietaire = ?";
         // Requête pour mettre à jour les informations du véhicule
        String updateVehiculeQuery = "UPDATE vehicules SET matricule = ? WHERE id_vehicule = ?";
        // Requête pour mettre à jour la relation dans la table POSSEDER
        String updatePossessionQuery = "UPDATE posseder SET date_debut_propriete = ?, date_fin_propriete = ? " +
                                       "WHERE id_proprietaire = ? AND id_vehicule = ?";
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Vérifier si la relation existe déjà dans la base
            try (PreparedStatement ps = conn.prepareStatement(checkExistingQuery)) {
                ps.setInt(1, idProprietaire);
                ps.setInt(2, idVehicule);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Si une relation existe déjà, on refuse la mise à jour
                    JOptionPane.showMessageDialog(null, "Cette relation existe déjà. Impossible de mettre à jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return; // Stoppe la mise à jour
                }
            }
    
            // Mise à jour du propriétaire
            try (PreparedStatement ps = conn.prepareStatement(updateProprietaireQuery)) {
                ps.setString(1, newNomProprietaire);
                ps.setString(2, newPrenomProprietaire);
                ps.setInt(3, idProprietaire);
                ps.executeUpdate();
            }
    
            // Mise à jour du véhicule
            try (PreparedStatement ps = conn.prepareStatement(updateVehiculeQuery)) {
                ps.setString(1, newMatriculeVehicule);
                ps.setInt(2, idVehicule);
                ps.executeUpdate();
            }
    
            // Mise à jour de la relation dans la table posseder
            try (PreparedStatement ps = conn.prepareStatement(updatePossessionQuery)) {
                ps.setDate(1, newDateDebut);
                ps.setDate(2, newDateFin); // La date de fin est optionnelle
                ps.setInt(3, idProprietaire);
                ps.setInt(4, idVehicule);
                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Relation mise à jour avec succès !");
                } else {
                    JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Une erreur s'est produite lors de la mise à jour de la relation. Détails : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    /**
    * Supprime une relation existante entre un propriétaire et un véhicule.
    *
    * @param idProprietaire L'identifiant du propriétaire
    * @param idVehicule     L'identifiant du véhicule
    */
    public void deletePossession(int idProprietaire, int idVehicule) {
        String query = "DELETE FROM posseder WHERE id_proprietaire = ? AND id_vehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Possession supprimée avec succès !");
            } else {
                JOptionPane.showMessageDialog(null, "Erreur lors de la suppression.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}