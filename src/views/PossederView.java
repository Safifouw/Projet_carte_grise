package views;

import controllers.PossederController;
import models.Posseder;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class PossederView extends JFrame {
    private PossederController controller;

    public PossederView() {
        controller = new PossederController();

        setTitle("Gestion des Possessions");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Disposition verticale

        // Charger les possessions
        List<Posseder> possessions = controller.getAllPossessions();
        for (Posseder possession : possessions) {
            // Créer un panel pour chaque possession
            JPanel possessionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel possessionLabel = new JLabel(
                    "Propriétaire: " + possession.getPrenom() + " " + possession.getNom() +
                    ", Véhicule: " + possession.getMatricule() +
                    ", Début: " + possession.getDateDebutPropriete() +
                    ", Fin: " + (possession.getDateFinPropriete() != null ? possession.getDateFinPropriete() : "Actuel")
            );

           // Bouton Modifier la relation
JButton modifyButton = new JButton("Modifier la relation");
modifyButton.addActionListener(e -> {
    // Création des champs pour les nouvelles valeurs
    JTextField nomProprietaireField = new JTextField(possession.getNom());
    JTextField prenomProprietaireField = new JTextField(possession.getPrenom());
    JTextField matriculeVehiculeField = new JTextField(possession.getMatricule());
    JTextField dateDebutField = new JTextField(possession.getDateDebutPropriete().toString());
    JTextField dateFinField = new JTextField(possession.getDateFinPropriete() != null ? possession.getDateFinPropriete().toString() : "");

    Object[] message = {
        "Nom du propriétaire :", nomProprietaireField,
        "Prénom du propriétaire :", prenomProprietaireField,
        "Matricule du véhicule :", matriculeVehiculeField,
        "Date de début (YYYY-MM-DD) :", dateDebutField,
        "Date de fin (YYYY-MM-DD) :", dateFinField
    };

    int option = JOptionPane.showConfirmDialog(this, message, "Modifier la relation", JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION) {
        try {
            // On récupère les nouvelles valeurs
            String newNomProprietaire = nomProprietaireField.getText();
            String newPrenomProprietaire = prenomProprietaireField.getText();
            String newMatriculeVehicule = matriculeVehiculeField.getText();
            Date newDateDebut = Date.valueOf(dateDebutField.getText());
            Date newDateFin = dateFinField.getText().isEmpty() ? null : Date.valueOf(dateFinField.getText());

            // Appel à la méthode de mise à jour
            controller.updatePossession(possession.getIdProprietaire(), possession.getIdVehicule(), newNomProprietaire, newPrenomProprietaire, newMatriculeVehicule, newDateDebut, newDateFin);

            // Actualisation de la vue
            refreshView();
        } catch (Exception ex) {
            showErrorMessage("Format de date invalide ou erreur lors de la mise à jour.");
        }
    }
});


            // Bouton Supprimer
            JButton deleteButton = new JButton("Supprimer");
            deleteButton.addActionListener(e -> {
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Êtes-vous sûr de vouloir supprimer cette possession ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        controller.deletePossession(possession.getIdProprietaire(), possession.getIdVehicule());
                        refreshView();
                    } catch (Exception ex) {
                        showErrorMessage("Erreur lors de la suppression de la possession.");
                    }
                }
            });

            // Ajouter les composants au panel
            possessionPanel.add(possessionLabel);
            possessionPanel.add(modifyButton);
            possessionPanel.add(deleteButton);
            panel.add(possessionPanel);
        }

        // Panel pour les boutons "Ajouter" et "Retour"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Bouton "Ajouter une Possession"
        JButton addButton = new JButton("Ajouter une Possession");
        addButton.addActionListener(e -> {
            JTextField nomProprietaireField = new JTextField();
            JTextField prenomProprietaireField = new JTextField();
            JTextField matriculeVehiculeField = new JTextField();
            JTextField dateDebutField = new JTextField();
            Object[] message = {
                    "Nom du propriétaire :", nomProprietaireField,
                    "Prénom du propriétaire :", prenomProprietaireField,
                    "Matricule du véhicule :", matriculeVehiculeField,
                    "Date de début (YYYY-MM-DD) :", dateDebutField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Ajouter une Possession", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String nomProprietaire = nomProprietaireField.getText();
                    String prenomProprietaire = prenomProprietaireField.getText();
                    String matriculeVehicule = matriculeVehiculeField.getText();
                    Date dateDebut = Date.valueOf(dateDebutField.getText());
                    controller.addPossession(nomProprietaire, prenomProprietaire, matriculeVehicule, dateDebut);
                    refreshView();
                } catch (Exception ex) {
                    showErrorMessage("Données invalides ou erreur lors de l'ajout.");
                }
            }
        });

        // Bouton "Retour"
        JButton backButton = new JButton("Retour");
        backButton.addActionListener(e -> dispose());

        // Ajouter les boutons au panel
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);

        // Ajouter le panel des boutons au panneau principal
        panel.add(buttonPanel);

        // Ajouter un JScrollPane pour la barre de défilement
        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane);

        // Rendre visible
        setVisible(true);
    }

    // Méthode pour rafraîchir la vue
    private void refreshView() {
        dispose();
        new PossederView(); // Recharger la vue avec les nouvelles possessions
    }

    // Afficher un message d'erreur
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        new PossederView();
    }
}
