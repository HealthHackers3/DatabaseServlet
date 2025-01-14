package api.debug;

import api.interfaces.apiCommandHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class getRefreshAllTables implements apiCommandHandler {

    public getRefreshAllTables() {
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        try {
            // Refresh all SQL tables to default in case we mess anything up
            // Step 1: Drop all tables in reverse order of dependencies
            String dropAllObjects =
                    "DO $$ " +
                            "DECLARE " +
                            "    rec RECORD; " +
                            "BEGIN" +
                            "    FOR rec IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP " +
                            "        EXECUTE 'DROP TABLE IF EXISTS \"' || rec.tablename || '\" CASCADE;'; " +
                            "    END LOOP;" +
                            "    FOR rec IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public') LOOP " +
                            "        EXECUTE 'DROP SEQUENCE IF EXISTS \"' || rec.sequence_name || '\" CASCADE;'; " +
                            "    END LOOP; " +
                            "END $$;";


            s.execute(dropAllObjects);

            // Step 2: Recreate tables
            s.execute("CREATE TABLE Lusers (" +
                    "user_id SERIAL PRIMARY KEY," +
                    "username VARCHAR(100) UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "email VARCHAR(255) UNIQUE NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            s.execute("CREATE TABLE Lcategories (" +
                    "category_id SERIAL PRIMARY KEY," +
                    "category_name VARCHAR(255) NOT NULL UNIQUE" +
                    ");");

            s.execute("CREATE TABLE Lcell_types (" +
                    "cell_type_id SERIAL PRIMARY KEY," +
                    "cell_type_name VARCHAR(255) NOT NULL UNIQUE" +
                    ");");

            s.execute("CREATE TABLE Limage_modalities (" +
                    "image_modality_id SERIAL PRIMARY KEY," +
                    "image_modality_name VARCHAR(255) NOT NULL UNIQUE" +
                    ");");

            s.execute("CREATE TABLE Lposts (" +
                    "post_id SERIAL PRIMARY KEY," +
                    "poster_id INT NOT NULL," +
                    "post_name VARCHAR(100) NOT NULL," +
                    "category_id INT NOT NULL," +
                    "cell_type_id INT NOT NULL," +
                    "image_modality_id INT NOT NULL," +
                    "category_user_picked VARCHAR(100) DEFAULT NULL," +
                    "cell_type_user_picked VARCHAR(100) DEFAULT NULL," +
                    "image_modality_user_picked VARCHAR(100) DEFAULT NULL," +
                    "description TEXT," +
                    "likes INT DEFAULT 0 CHECK (likes >= 0)," +
                    "upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (poster_id) REFERENCES Lusers(user_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (category_id) REFERENCES Lcategories(category_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (cell_type_id) REFERENCES Lcell_types(cell_type_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (image_modality_id) REFERENCES Limage_modalities(image_modality_id) ON DELETE CASCADE" +
                    ");");

            s.execute("CREATE TABLE Lpost_images (" +
                    "image_id SERIAL PRIMARY KEY," +
                    "post_id INT NOT NULL," +
                    "order_index INT NOT NULL," +
                    "image_file_name TEXT NOT NULL," +
                    "cell_count INT DEFAULT 0 CHECK (cell_count >= 0)," +
                    "cell_dimensions_y INT DEFAULT 0 CHECK (cell_dimensions_y >= 0)," +
                    "cell_dimensions_x INT DEFAULT 0 CHECK (cell_dimensions_x >= 0)," +
                    "cell_density INT DEFAULT 0 CHECK (cell_density >= 0)," +
                    "image_path TEXT," +
                    "FOREIGN KEY (post_id) REFERENCES Lposts(post_id) ON DELETE CASCADE" +
                    ");");

            s.execute("CREATE TABLE Lpost_images_thumbnails (" +
                    "thumbnail_id SERIAL PRIMARY KEY," +
                    "post_id INT NOT NULL," +
                    "ref_image_id INT NOT NULL," +
                    "image_path TEXT NOT NULL," +
                    "FOREIGN KEY (post_id) REFERENCES Lposts(post_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (ref_image_id) REFERENCES Lpost_images(image_id)" +
                    ");");

            s.execute("CREATE TABLE Lpost_likes (" +
                    "user_id INT NOT NULL," +
                    "post_id INT NOT NULL," +
                    "UNIQUE (user_id, post_id)," +
                    "PRIMARY KEY(user_id, post_id)," +
                    "FOREIGN KEY (post_id) REFERENCES Lposts(post_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (user_id) REFERENCES Lusers(user_id) ON DELETE CASCADE" +
                    ");");

            s.execute("CREATE TABLE Luser_profile_pics (" +
                    "user_id INT PRIMARY KEY," +
                    "profile_pic BYTEA NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES Lusers(user_id) ON DELETE CASCADE" +
                    ");");

            s.execute("CREATE TABLE Ltags (" +
                    "tag_id SERIAL PRIMARY KEY," +
                    "tag_name VARCHAR(255) NOT NULL UNIQUE" +
                    ");");

            s.execute("CREATE TABLE Lsessions (" +
                    "session_id SERIAL PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "session_token TEXT NOT NULL UNIQUE," +
                    "expires_at TIMESTAMP NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES Lusers(user_id) ON DELETE CASCADE" +
                    ");");
            //insert a dummy user and post
            s.execute("INSERT INTO Lusers (user_id, username, password, email, created_at) " +
                    "VALUES (-1, 'dummy_user', 'dummy_password', 'dummy_user@example.com', CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (user_id) DO NOTHING;");

            s.execute("INSERT INTO Lposts (post_id, poster_id, post_name, category_id, cell_type_id, image_modality_id, description, likes, upload_date) " +
                    "VALUES (-1, -1, 'Dummy Post', 2, 2, 2, 'This post is used to store temporary images before being attached to a post', 0, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (post_id) DO NOTHING;");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS Lposts_composite (" +
                            "    post_id INT NOT NULL," +
                            "    post_search_key TEXT," +
                            "    search_vector tsvector," +
                            "    FOREIGN KEY (post_id) REFERENCES Lposts(post_id) ON DELETE CASCADE" +
                            ");"
            );
            s.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm;");


            s.execute("DO $$ DECLARE " +
                    "category_list TEXT := 'Other,Eukarya,Bacteria,Archaea,Viruses,Prions';" +
                    "category_item TEXT; " +
                    "BEGIN " +
                    "FOR category_item IN SELECT unnest(string_to_array(category_list, ',')) LOOP " +
                    "INSERT INTO Lcategories (category_name) VALUES (category_item) " +
                    "ON CONFLICT (category_name) DO NOTHING; " +
                    "END LOOP; " +
                    "END $$;");

            s.execute("DO $$ DECLARE " +
                    "cell_type_list TEXT := 'Other,Adipocyte,Basophil,Cardiomyocyte,Chondrocyte,Erythrocyte,Fibroblast,Hepatocyte,Keratinocyte,Langerhans cell,Macrophage,Mast cell,Melanocyte,Monocyte,Myocyte(skeletal),Myocyte(smooth),Myocyte(cardiac),Neutrophil,Osteoblast,Osteoclast,Pancreatic beta cell,Platelet,Schwann cell,T-cell,B-cell,Natural killer cell,Spermatocyte,Oocyte,Retinal ganglion cell,Cone cell,Rod cell,Ependymal cell,Endothelial cell,Goblet cell,Granulocyte,Thrombocyte,Lymphocyte,Microglial cell,Satellite cell,Parenchyma cell,Collenchyma cell,Sclerenchyma cell,Guard cell,Companion cell,Sieve-tube element,Tracheid,Vessel element,Xylem fiber,Phloem fiber,Cork cell,Epidermal cell,Root hair cell,Palisade mesophyll cell,Spongy mesophyll cell,Chlorenchyma cell,Endodermal cell,Meristematic cell,Coccus,Bacillus,Spirillum,Spirochete,Vibrio,Filamentous bacteria,Actinomycete,Protist(amoeboid),Protist(flagellate),Protist(ciliate),Fungi(hyphal cell),Fungi(yeast cell),Fungi(spore-forming cell),DNA virus,RNA virus,Retrovirus,Bacteriophage,Lipid-enveloped virus,Non-enveloped virus,Icosahedral virus,Helical virus,Complex virus,Satellite virus,Prion-like particles,Archaea(methanogen),Archaea(halophile),Alveolar cell,Intestinal enterocyte,Bundle sheath cell,Transfer cell,Reproductive cell(egg cell),Reproductive cell(pollen cell),Oligodendrocyte,Astrocyte';" +
                    "cell_type_item TEXT; " +
                    "BEGIN " +
                    "FOR cell_type_item IN SELECT unnest(string_to_array(cell_type_list, ',')) LOOP " +
                    "INSERT INTO Lcell_types (cell_type_name) VALUES (cell_type_item) " +
                    "ON CONFLICT (cell_type_name) DO NOTHING; " +
                    "END LOOP; " +
                    "END $$;");

            s.execute("DO $$ DECLARE " +
                    "image_modality_list TEXT := 'Other, Fluorescence microscopy (live-cell),Fluorescence microscopy (fixed-cell),Confocal microscopy,Two-photon microscopy,Super-resolution microscopy (STED),Super-resolution microscopy (PALM/STORM),Phase-contrast microscopy,Differential interference contrast (DIC) microscopy,Raman microscopy,Fluorescence in situ hybridization (FISH),Live-cell imaging (time-lapse),High-content screening imaging,Fluorescent lifetime imaging (FLIM),Total internal reflection fluorescence (TIRF) microscopy,Structured illumination microscopy (SIM),Light-sheet microscopy,Spinning disk confocal microscopy,Widefield fluorescence microscopy,Multiphoton microscopy,Optical coherence tomography (OCT),Electron microscopy (SEM),Electron microscopy (TEM),Cryo-electron microscopy (Cryo-EM),Dynamic light scattering (DLS),Flow cytometry (imaging),Mass cytometry (imaging),Label-free microscopy (holographic),Atomic force microscopy (AFM),Spectral imaging (hyperspectral)';" +
                    "image_modality_item TEXT; " +
                    "BEGIN " +
                    "FOR image_modality_item IN SELECT unnest(string_to_array(image_modality_list, ',')) LOOP " +
                    "INSERT INTO Limage_modalities(image_modality_name) VALUES (image_modality_item) " +
                    "ON CONFLICT (image_modality_name) DO NOTHING; " +
                    "END LOOP; " +
                    "END $$;");

            // Success response
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\": \"Tables reset successfully\"}");
        } catch (Exception e) {
            handleError(resp, "Failed to reset tables:" + e.getMessage(), e);
        }
    }
}
