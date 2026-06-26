public class builder {
    static class Maggie {
        // compulsary
        private  final Boolean ingredient;
        private  final Boolean oil;

        // optional
        private  final Boolean water;
        private  final Boolean vegees;
        private  final Boolean butter;
        private  final Boolean spices;

        public   Maggie(IngredientsRequired builder) {
            this.ingredient = builder.ingredient;
            this.oil = builder.oil;
            this.water = builder.water;
            this.vegees = builder.vegees;
            this.butter = builder.butter;
            this.spices = builder.spices;

        }

        @Override
        public String toString() {
            return "Maggie{" +
                    "ingredient=" + ingredient +
                    ", oil=" + oil +
                    ", water=" + water +
                    ", vegees=" + vegees +
                    ", butter=" + butter +
                    ", spices=" + spices +
                    '}';
        }

        static public class IngredientsRequired {
            // compulsary
            private Boolean ingredient;
            private Boolean oil;

            // optional
            private Boolean water;
            private Boolean vegees;
            private Boolean butter;
            private Boolean spices;

            public IngredientsRequired(Boolean ingredient, Boolean oil) {
                this.ingredient = ingredient;
                this.oil = oil;

            }

            public IngredientsRequired addWater(Boolean water) {
                this.water = water;
                return this;

            }

            public IngredientsRequired addVegees(Boolean vegees) {
                this.vegees = vegees;
                return this;

            }

            public IngredientsRequired addButtter(Boolean butter) {
                this.butter = butter;
                return this;

            }

            public IngredientsRequired addSpices(Boolean spices) {
                this.spices = spices;
                return this;

            }

            public Maggie execute() {

                return new Maggie(this);

            }

        }

    }

    public static void main(String[] str) {
       Maggie meal = new Maggie.IngredientsRequired(true,true).addWater(true).execute();
       System.out.println(meal);


    }

}
